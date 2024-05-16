import mongoose from "mongoose";
import pointSchema from "./pointSchema.js";

const carSchema = new mongoose.Schema(
  {
    brand: {
      type: String,
      required: true,
    },
    model: {
      type: String,
      required: true,
    },
    year: {
      type: Number,
      required: true,
    },
    totalKm: {
      type: Number,
      required: true,
    },
    isElectric: {
      type: Boolean,
      required: true,
    },
    location: {
      type: pointSchema,
      index: "2dsphere",
    },
    inUse: {
      type: Boolean,
      default: false,
    },
    carpic: {
      type: String,
      default: "public/photos/defaultAvatar.jpg",
    },
    previousRides: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: "Rides",
      },
    ],
  },
  {
    timestamps: true,
  }
);

const CarModel = mongoose.model("Car", carSchema);

export default CarModel;
