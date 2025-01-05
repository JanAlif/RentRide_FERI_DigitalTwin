import mongoose from "mongoose";
import pointSchema from "./pointSchema.js";
import lineSchema from "./lineSchema.js";
import crypto from "crypto"; // For hash generation

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
    cost: {
      type: Number,
      required: true,
    },
    previousHash: {
      type: String,
      default: null, // Null for the first blockchain-eligible ride
    },
    currentHash: {
      type: String,
    },
  },
  {
    timestamps: true,
  }
);

// Helper to generate a hash
rideSchema.methods.generateHash = function () {
  const data = `${this.driver}${this.car}${JSON.stringify(
    this.startLocation
  )}${JSON.stringify(this.endLocation)}${this.startTime}${this.endTime}${this.status}${this.cost}${this.previousHash}`;
  return crypto.createHash("sha256").update(data).digest("hex");
};

// Pre-save middleware to compute the current hash
rideSchema.pre("save", function (next) {
  if (this.cost) {
    // Generate the hash only for rides with a cost
    this.currentHash = this.generateHash();
  }
  next();
});

const RideModel = mongoose.model("Ride", rideSchema);

export default RideModel;
