import asyncHandler from "express-async-handler";
import UserModel from "../models/userModel.js";
import generateToken from "../utils/generateToken.js";

//authenticates user and sends JWT
//route POST /api/users/login
const loginUser = asyncHandler(async (req, res) => {
  const { username, password } = req.body;
  const user = await UserModel.findOne({ username });
  if (user && (await user.matchPassword(password))) {
    generateToken(res, user._id);
    res.status(201).json({
      _id: user._id,
      username: user.username,
      email: user.email,
    });
  } else {
    res.status(401);
    throw new Error("Invalid username or password");
  }
});

//registers user, saves user to DB, generates JWT
//route POST /api/users/register
const registerUser = asyncHandler(async (req, res) => {
  const { username, email, password } = req.body;
  const emailExists = await UserModel.findOne({ email });
  if (emailExists) {
    res.status(400);
    throw new Error("Email already used.");
  }
  const usernameExists = await UserModel.findOne({ username });
  if (usernameExists) {
    res.status(400);
    throw new Error("Username already used.");
  }

  const user = await UserModel.create({
    username,
    email,
    password,
  });

  if (user) {
    generateToken(res, user._id);
    res.status(201).json({
      _id: user._id,
      username: user.username,
      email: user.email,
    });
  } else {
    res.status(400);
    throw new Error("Invalid user data");
  }
});

//logs out user, deletes Cookie
//POST /api/users/logout
const logoutUser = asyncHandler(async (req, res) => {
  res.cookie("jwt", "", {
    httpOnly: true,
    expires: new Date(0),
  });

  res.status(200).json({ message: "User logged out" });
});

//GET /api/users
const getAllUsers = asyncHandler(async (req, res) => {
  const users = await UserModel.find({});
  if (users) {
    res.status(200).json(users);
  } else {
    res.status(404).json({ message: "No users found" });
    throw new Error("No users found");
  }
});

//GET /api/users/:id
const getUserById = asyncHandler(async (req, res) => {
  const user = await UserModel.findById(req.params.id);
  if (user) {
    res.status(200).json(user);
  } else {
    res.status(404);
    throw new Error("User not found");
  }
});

//updates user info
//PUT /api/users/:id
const updateUser = asyncHandler(async (req, res) => {
  const user = await UserModel.findById(req.params.id);
  if (user) {
    user.username = req.body.username || user.username;
    user.email = req.body.email || user.email;
    user.name = req.body.name || user.name;
    user.surname = req.body.surname || user.surname;
    user.profilepic = req.body.profilepic || user.profilepic;
    if (req.body.longitude && req.body.latitude) {
      user.location = {
        type: "Point",
        coordinates: [req.body.longitude, req.body.latitude],
      };
    } else {
      user.location = user.location;
    }

    const updatedUser = await user.save();
    res.json({
      _id: updatedUser._id,
      username: updatedUser.username,
      email: updatedUser.email,
      name: updatedUser.name,
      surname: updatedUser.surname,
      profilepic: updatedUser.profilepic,
    });
  } else {
    res.status(404);
    throw new Error("User not found");
  }
});

//removes a user from the DB
//DELETE /api/users/:id
const deleteUser = asyncHandler(async (req, res) => {
  const user = await UserModel.findByIdAndDelete(req.params.id);
  if (user) {
    res.json({ message: "User removed" });
  } else {
    res.status(404);
    throw new Error("User not found");
  }
});

export {
  registerUser,
  loginUser,
  logoutUser,
  getAllUsers,
  getUserById,
  updateUser,
  deleteUser,
};
