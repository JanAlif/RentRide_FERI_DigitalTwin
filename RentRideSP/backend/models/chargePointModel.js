import mongoose from "mongoose";
import pointSchema from "./pointSchema.js";

const chargePointSchema = new mongoose.Schema(
  {
    locationName: {
      type: String,
      required: true,
    },
    address: {
      type: String,
      required: true,
    },
    location: {
      type: pointSchema,
      index: "2dsphere",
      required: true,
    },
    connectors: {
      type: Number,
      required: true,
    },
    connectorsAvailable: {
      type: Number,
      required: true,
    },
  },
  {
    timestamps: true,
  }
);

const ChargePointModel = mongoose.model("ChargePoint", chargePointSchema);

export default ChargePointModel;
