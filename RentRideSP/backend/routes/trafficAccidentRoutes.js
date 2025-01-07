import express from "express";
import {
  addAccident,
  getAllAccidents,
  getAccidentsBeforeDate,
  addAccidentFromURL,
  getAccidentsAfterDate, // Import the URL-based addition controller
} from "../controllers/trafficAccidentController.js";

const router = express.Router();

// Routes for traffic accidents
router.get("/", getAllAccidents); // Retrieve all accidents
router.get("/before", getAccidentsBeforeDate); // Retrieve accidents before a specific date
router.get("/add", addAccidentFromURL); // Add an accident via query parameters
router.get("/after", getAccidentsAfterDate); // Retrive accidents after specific date
router.post("/", addAccident); // Add a new accident (JSON body)

export default router;