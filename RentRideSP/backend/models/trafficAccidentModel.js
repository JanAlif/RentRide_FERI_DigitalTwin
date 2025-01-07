import mongoose from "mongoose";
import crypto from "crypto"; // For hash generation

const trafficAccidentSchema = new mongoose.Schema(
  {
    title: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      required: true,
    },
    coordinates: {
      type: { type: String, default: "Point" },
      coordinates: {
        type: [Number], // [longitude, latitude]
        required: true,
      },
    },
    user: {
      type: String, // Simple string instead of ObjectId reference
      required: true,
    },
    time: {
      type: Date,
      default: Date.now,
      required: true,
    },
    force: {
      type: Number,
      required: true,
    },
    previousHash: {
      type: String,
      default: null, // Null for the first blockchain-eligible accident
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
trafficAccidentSchema.methods.generateHash = function () {
  const data = `${this.title}${this.description}${JSON.stringify(
    this.coordinates
  )}${this.user}${this.time}${this.force}${this.previousHash}`;
  return crypto.createHash("sha256").update(data).digest("hex");
};

// Pre-save middleware to compute the current hash
trafficAccidentSchema.pre("save", function (next) {
  this.currentHash = this.generateHash();
  next();
});

const TrafficAccidentModel = mongoose.model("TrafficAccident", trafficAccidentSchema);

export default TrafficAccidentModel;