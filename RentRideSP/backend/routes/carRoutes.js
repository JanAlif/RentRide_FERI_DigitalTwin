import express from "express";
import { protect } from "../middleware/authMiddleware.js";
import {
  getAllCars,
  getCarById,
  addCar,
  updateCar,
  deleteCar,
  getChargePointsNearby,
  updateCarStatus ,
  updateCarDetails,
} from "../controllers/carController.js";

const router = express.Router();

router.get("/", getAllCars);
router.get("/chargepointsnearby", getChargePointsNearby);
router.get("/:id", getCarById);
router.post("/", addCar);
router.put("/:id", updateCar);
router.delete("/:id", deleteCar);
router.patch('/:id/status', updateCarStatus);
router.route('/:id/details').patch(updateCarDetails);

export default router;
