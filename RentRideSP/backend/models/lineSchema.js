import mongoose from "mongoose";

const lineSchema = new mongoose.Schema({
  type: {
    type: String,
    enum: ["LineString"],
    required: true,
  },
  coordinates: {
    type: [[Number]],
    default: [
      [0, 0],
      [1, 1],
    ],
    required: true,
  },
});

export default lineSchema;
