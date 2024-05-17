import express from "express";
import dotenv from "dotenv";
import connectDB from "./config/db.js";
import cookieParser from "cookie-parser";
import morgan from "morgan";
import { notFound, errorHandler } from "./middleware/errorMiddleware.js";
import userRoutes from "./routes/userRoutes.js";
import carsRoutes from "./routes/carRoutes.js";
import chargePointRoutes from "./routes/chargePointRoutes.js";
import ridesRouter from "./routes/rideRoutes.js";
import http from "http";
import { Server } from "socket.io";
import path from "path";
import { dirname } from "path";

const app = express();
const server = http.createServer(app);
const io = new Server(server);

io.on("connection", (socket) => {
  console.log("socket connected");
  socket.on("disconnect", () => {
    console.log("socket disconnected");
  });
});

dotenv.config();
const port = process.env.PORT || 4000;
app.listen(port, () => console.log(`Server started on ${port}`));
connectDB();

app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

//socket TEST
app.get("/", (req, res) => {
  const __dirname = path.resolve(path.dirname(""));
  res.sendFile(path.join(__dirname, "sockettest.html"));
});

//Routes
app.use("/api/users", userRoutes);
app.use("/api/cars", carsRoutes);
app.use("/api/chargepoints", chargePointRoutes);
app.use("/api/rides", ridesRouter);

//Error middleware
app.use(notFound);
app.use(errorHandler);
