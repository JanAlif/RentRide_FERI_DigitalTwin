import express from "express";
import { protect } from "../middleware/authMiddleware.js";
import {
  getAllCars,
  getCarById,
  addCar,
  updateCar,
  deleteCar,
  getChargePointsNearby,
} from "../controllers/carController.js";

const router = express.Router();

router.get("/", getAllCars);
router.get("/chargepointsnearby", getChargePointsNearby);
router.get("/:id", getCarById);
router.post("/", addCar);
router.put("/:id", updateCar);
router.delete("/:id", deleteCar);

export default router;
