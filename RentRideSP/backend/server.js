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
import trafficAccidentRoutes from "./routes/trafficAccidentRoutes.js";
import { Socket, Server } from 'socket.io';
import { addAccident} from './controllers/trafficAccidentController.js';
import http from 'http';
import mqtt from 'mqtt';

const app = express();

dotenv.config();
const port = process.env.PORT || 4000;

connectDB();

app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

//Routes
app.use("/api/users", userRoutes);
app.use("/api/cars", carsRoutes);
app.use("/api/chargepoints", chargePointRoutes);
app.use("/api/rides", ridesRouter);
app.use("/api/accidents", trafficAccidentRoutes);

//Error middleware
app.use(notFound);
app.use(errorHandler);

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

// MQTT Setup
const mqttClient = mqtt.connect("mqtt://localhost:1883"); // Replace with your MQTT broker URL

mqttClient.on("connect", () => {
    console.log("Connected to MQTT broker");

    // Subscribe to relevant topics
    mqttClient.subscribe("car/put", (err) => {
        if (!err) {
            console.log("Subscribed to topic: car/update");
        }
    });
});

mqttClient.on("message", (topic, message) => {
    console.log(`Received message on topic ${topic}: ${message.toString()}`);

    if (topic === "car/put") {
        try {
            const data = JSON.parse(message.toString());
            console.log("Processing car update:", data);

            addAccident(null, null, accidentData);

            // Example: Emit an event to clients via Socket.IO
            io.emit("carUpdated", data);

            // Optionally, perform additional logic such as updating a database
        } catch (error) {
            console.error("Failed to process MQTT message:", error.message);
        }
    }
});

// Start the server
server.listen(port, '0.0.0.0', () => console.log(`Server started on port ${port}`));