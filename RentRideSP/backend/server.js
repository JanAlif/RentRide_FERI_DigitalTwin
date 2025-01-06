// backend/server.js

import express from "express";
import dotenv from "dotenv";
import connectDB from "./config/db.js";
import cookieParser from "cookie-parser";
import morgan from "morgan";
import { notFound, errorHandler } from "./middleware/errorMiddleware.js";
import socketAuthMiddleware from './middleware/socketAuthMiddleware.js';
import userRoutes from "./routes/userRoutes.js";
import carsRoutes from "./routes/carRoutes.js";
import chargePointRoutes from "./routes/chargePointRoutes.js";
import ridesRouter from "./routes/rideRoutes.js";
import { Server } from 'socket.io';
import http from 'http';
import cron from 'node-cron';
import { spawn } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';
import Traffic from './models/Traffic.js'; // Import the Traffic model
import trafficRoutes from './routes/trafficRoutes.js'; // Import Traffic Routes

dotenv.config();

// For ES modules __dirname equivalent
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

const port = process.env.PORT || 4000;

connectDB();

app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

// Routes
app.use("/api/users", userRoutes);
app.use("/api/cars", carsRoutes);
app.use("/api/chargepoints", chargePointRoutes);
app.use("/api/rides", ridesRouter);
app.use('/api/traffics', trafficRoutes); // Use Traffic Routes

// Error middleware
app.use(notFound);
app.use(errorHandler);

// Create HTTP server
const server = http.createServer(app);

// Setup Socket.IO
const io = new Server(server, {
    cors: {
        origin: '*', // Replace with your client URL
    },
});

io.use(socketAuthMiddleware);

io.on('connection', (socket) => {
    console.log('A user connected:', socket.user);

    if (socket.user.role === 'admin') {
        socket.on('createRoom', (room) => {
            console.log('Room created:', room);
            socket.join(room);
            socket.emit('roomCreated', room);
        });

        socket.on('emitToRoom', (data) => {
            const { room, message } = data;
            io.to(room).emit('message', message);
        });
        socket.on('disconnect', () => {
            // 'room' is undefined here; consider storing room info if needed
            console.log('Admin disconnected');
        });
    } else if (socket.user.role === 'user') {
        socket.on('joinRoom', (room) => {
            const rooms = io.sockets.adapter.rooms;
            if (rooms.has(room)) {
                socket.join(room);
                socket.emit('roomJoined', room);
                console.log(`User joined room: ${room}`);
            } else {
                socket.emit('error', 'Room does not exist');
                console.log(`User tried to join non-existent room: ${room}`);
            }
        });

        socket.on('disconnect', () => {
            console.log('User disconnected');
        });
    }
});

// Function to execute the Python script
const runDetectionScript = () => {
    // Path to the Python script
    const scriptPath = path.join(__dirname, 'detection.py');

    // Directory where the script is located
    const scriptDir = path.dirname(scriptPath);

    // Spawn a child process to run the Python script with the correct working directory
    const pythonProcess = spawn('python3', [scriptPath], { cwd: scriptDir });

    let dataString = '';
    let errorString = '';

    // Collect data from stdout
    pythonProcess.stdout.on('data', (data) => {
        dataString += data.toString();
    });

    // Collect data from stderr
    pythonProcess.stderr.on('data', (data) => {
        errorString += data.toString();
    });

    pythonProcess.on('close', async (code) => {
        if (code !== 0) {
            console.error(`Python script exited with code ${code}`);
            console.error(`Stderr: ${errorString}`);
            return;
        }

        try {
            // Attempt to parse the JSON output
            const result = JSON.parse(dataString);
            if (result.status === 'success') {
                console.log(`Detections:`, result.results);
                
                // Iterate over each detection result
                for (const detection of result.results) {
                    const { imageUrl, detectedVehicles, classificationResult, coordinates } = detection;

                    // Validate coordinates
                    if (!coordinates || !Array.isArray(coordinates)) {
                        console.error(`Invalid coordinates for image: ${imageUrl}`);
                        continue; // Skip this entry
                    }

                    // Validate each coordinate pair
                    const validCoordinates = coordinates.filter(coord => {
                        return (
                            Array.isArray(coord) &&
                            coord.length === 2 &&
                            typeof coord[0] === 'number' &&
                            typeof coord[1] === 'number' &&
                            coord[0] >= -90 && coord[0] <= 90 &&
                            coord[1] >= -180 && coord[1] <= 180
                        );
                    });

                    if (validCoordinates.length === 0) {
                        console.error(`No valid coordinates for image: ${imageUrl}`);
                        continue; // Skip this entry
                    }

                    // Transform coordinates into objects with latitude and longitude
                    const coordinatesObjects = validCoordinates.map(coord => ({
                        latitude: coord[0],
                        longitude: coord[1],
                    }));

                    // Use findOneAndUpdate with upsert option
                    await Traffic.findOneAndUpdate(
                        { imageUrl }, // Filter by imageUrl
                        {
                            detectedVehicles,
                            classificationResult,
                            coordinates: coordinatesObjects, // Save array of coordinates
                            timestamp: new Date(), // Update timestamp
                        },
                        { upsert: true, new: true, setDefaultsOnInsert: true }
                    );

                    console.log(`Updated classificationResult and coordinates for image: ${imageUrl}`);
                }

                // Optionally, emit via Socket.IO
                io.emit('detectionResults', result.results);
            } else {
                console.error(`Detection error: ${result.message}`);
            }
        } catch (error) {
            console.error('Error parsing Python script output:', error);
            console.error('Output was:', dataString);
        }
    });

    // Handle errors when spawning the process
    pythonProcess.on('error', (err) => {
        console.error('Failed to start subprocess:', err);
    });
};

// Schedule the detection script to run every minute
cron.schedule('* * * * *', () => {
    console.log('Running detection script...');
    runDetectionScript();
});

// Optionally, run the script immediately on server start
runDetectionScript();

// Start the server
server.listen(port, '0.0.0.0', () => console.log(`Server started on port ${port}`));