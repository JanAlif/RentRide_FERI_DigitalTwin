import express from "express";
import { protect } from "../middleware/authMiddleware.js";
import {
  registerUser,
  loginUser,
  logoutUser,
  getAllUsers,
  getUserById,
  updateUser,
  deleteUser,
  updateUserPassword,
  uploadProfilePic,
} from "../controllers/userController.js";

const router = express.Router();

router.get("/", getAllUsers);
router.get("/:id", getUserById);

router.post("/register", registerUser);
router.post("/login", loginUser);
router.post("/logout", protect, logoutUser);
router.post("/:id/profile-pic", protect, uploadProfilePic); 

//tu sem zbrisal protect izmed :id in updateUser
router.put("/:id", updateUser);
router.put("/:id/password", protect, updateUserPassword);
router.delete("/:id", protect, deleteUser);

export default router;
