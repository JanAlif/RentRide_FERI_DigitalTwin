import express from "express";
import {
  addChargePoint,
  getAllChargePoints,
  updateChargePoint,
  getChargePointById,
  deleteChargePoint,
} from "../controllers/chargePointController.js";

const router = express.Router();

router.post("/", addChargePoint);
router.get("/", getAllChargePoints);
router.get("/:id", getChargePointById);
router.put("/:id", updateChargePoint);
router.delete("/:id", deleteChargePoint);

export default router;
