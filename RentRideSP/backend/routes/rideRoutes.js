import express from "express";
import { protect } from "../middleware/authMiddleware.js";
import {
  addRide,
  getRideById,
  getAllRides,
  updateRide,
  deleteRide,
} from "../controllers/rideController.js";

const router = express.Router();

router.get("/", getAllRides);
router.get("/:id", getRideById);

router.post("/", protect, addRide);
router.put("/:id", protect, updateRide);
router.delete("/:id", protect, deleteRide);

export default router;
