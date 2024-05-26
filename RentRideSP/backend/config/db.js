import mongoose, { mongo } from "mongoose";

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.VITE_MONGO_URI);
    console.log(`MongoDB connected: ${conn.connection.host}`);
  } catch (error) {
    console.error(`${error.message}`);
    process.exit(1);
  }
};

export default connectDB;


