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

const app = express();

dotenv.config();
const port = process.env.PORT || 4000;
app.listen(port,'0.0.0.0', () => console.log(`Server started on ${port}`));
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

//Error middleware
app.use(notFound);
app.use(errorHandler);
