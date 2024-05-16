import mongoose from "mongoose";
import pointSchema from "./pointSchema.js";
import lineSchema from "./lineSchema.js";

const rideSchema = new mongoose.Schema(
  {
    driver: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    car: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Car",
      required: true,
    },
    startLocation: {
      type: pointSchema,
      index: "2dsphere",
      required: true,
    },
    endLocation: {
      type: pointSchema,
      index: "2dsphere",
      required: true,
    },
    path: {
      type: lineSchema,
    },
    startTime: {
      type: Date,
      default: Date.now,
      required: true,
    },
    endTime: {
      type: Date,
    },

    status: {
      type: String,
      enum: ["pending", "started", "completed", "cancelled"],
      default: "pending",
    },
  },
  {
    timestamps: true,
  }
);

const RideModel = mongoose.model("Ride", rideSchema);

export default RideModel;
