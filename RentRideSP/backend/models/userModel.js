import mongoose from "mongoose";
import bcrypt from "bcryptjs";
import pointSchema from "./pointSchema.js";

const userSchema = new mongoose.Schema(
  {
    username: {
      type: String,
      required: true,
      unique: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
    },
    name: {
      type: String,
    },
    surname: {
      type: String,
    },
    password: {
      type: String,
      required: true,
    },
    location: {
      type: pointSchema,
      index: "2dsphere",
    },
    profilepic: {
      type: String,
      default: "public/photos/defaultAvatar.jpg",
    },
    previousRides: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: "Rides",
      },
    ],
    credit: {
      type: Number,
      default: 0,
    },
  },
  {
    timestamps: true,
  }
);

userSchema.pre("save", async function (next) {
  if (!this.isModified("password")) {
    next();
  }
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
});

userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

const UserModel = mongoose.model("User", userSchema);

export default UserModel;
