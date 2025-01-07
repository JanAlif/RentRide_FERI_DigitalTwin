import asyncHandler from "express-async-handler";
import TrafficAccidentModel from "../models/trafficAccidentModel.js";

// Add a new traffic accident
// POST /api/accidents
const addAccident = asyncHandler(async (req, res) => {
  const { title, description, coordinates, user, time, force } = req.body;

  try {
    // Validate the blockchain
    const isBlockchainValid = await validateBlockchain();
    if (!isBlockchainValid) {
      return res.status(500).json({ message: "Blockchain validation failed. Accident cannot be added." });
    }

    // Blockchain logic: Get the last blockchain-eligible accident
    let previousHash = null;
    const lastAccident = await TrafficAccidentModel.findOne().sort({ createdAt: -1 });
    previousHash = lastAccident ? lastAccident.currentHash : null;

    const accident = new TrafficAccidentModel({
      title,
      description,
      coordinates: { type: "Point", coordinates },
      user,
      time,
      force,
      previousHash,
    });

    await accident.save();

    res.status(201).json(accident);
  } catch (error) {
    console.error("Error adding accident:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
});

// Get all traffic accidents
// GET /api/accidents
const getAllAccidents = asyncHandler(async (req, res) => {
  try {
    const accidents = await TrafficAccidentModel.find({});
    res.status(200).json(accidents);
  } catch (error) {
    console.error("Error retrieving accidents:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
});

// Get accidents from the last five days or earlier than a specific date
// GET /api/accidents?before=<date>
const getAccidentsBeforeDate = asyncHandler(async (req, res) => {
  const { before } = req.query;

  try {
    const date = before ? new Date(before) : new Date(Date.now() - 5 * 24 * 60 * 60 * 1000);
    const accidents = await TrafficAccidentModel.find({ time: { $lte: date } });
    res.status(200).json(accidents);
  } catch (error) {
    console.error("Error retrieving accidents before date:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
});


//GET /api/accidents/after?after=2025-01-01
const getAccidentsAfterDate = asyncHandler(async (req, res) => {
    const { after } = req.query;
  
    try {
      // Parse the provided date or default to the current time
      const date = after ? new Date(after) : new Date();
  
      // Fetch accidents that occurred after the specified date
      const accidents = await TrafficAccidentModel.find({ time: { $gte: date } });
  
      // Respond with the fetched accidents
      res.status(200).json(accidents);
    } catch (error) {
      console.error("Error retrieving accidents after date:", error);
      res.status(500).json({ message: "Server error", error: error.message });
    }
  });

// Add accident via URL query string
const addAccidentFromURL = asyncHandler(async (req, res) => {
    const { title, description, longitude, latitude, user, time, force } = req.query;
  
    // Validate required fields
    if (!title || !description || !longitude || !latitude || !user || !force) {
      return res.status(400).json({ message: "Missing required fields" });
    }
  
    try {
        // Validate the blockchain
        const isBlockchainValid = await validateBlockchain();
        if (!isBlockchainValid) {
            return res.status(500).json({ message: "Blockchain validation failed. Accident cannot be added." });
        }
      // Blockchain logic: Get the last accident for previous hash
      const lastAccident = await TrafficAccidentModel.findOne().sort({ createdAt: -1 });
      const previousHash = lastAccident ? lastAccident.currentHash : null;
  
      // Create a new accident
      const accident = new TrafficAccidentModel({
        title,
        description,
        coordinates: { type: "Point", coordinates: [parseFloat(longitude), parseFloat(latitude)] },
        user,
        time: time || new Date(),
        force: parseFloat(force),
        previousHash,
      });
  
      await accident.save();
  
      res.status(201).json(accident);
    } catch (error) {
      console.error("Error adding accident:", error);
      res.status(500).json({ message: "Server error", error: error.message });
    }
  });

// Validate blockchain
const validateBlockchain = async () => {
  const accidents = await TrafficAccidentModel.find({}).sort({ createdAt: 1 });

  for (let i = 1; i < accidents.length; i++) {
    const previousAccident = accidents[i - 1];
    const currentAccident = accidents[i];

    if (!currentAccident.previousHash || !currentAccident.currentHash) {
      console.warn(`Skipping non-compliant accident ${currentAccident._id}`);
      continue;
    }

    // Check if the `previousHash` matches the `currentHash` of the previous block
    if (currentAccident.previousHash !== previousAccident.currentHash) {
      console.error(`Blockchain broken between accidents ${previousAccident._id} and ${currentAccident._id}`);
      return false;
    }

    // Check if the `currentHash` matches the calculated hash
    const calculatedHash = currentAccident.generateHash();
    if (currentAccident.currentHash !== calculatedHash) {
      console.error(`Invalid hash for accident ${currentAccident._id}. Calculated hash does not match current hash.`);
      return false;
    }
  }

  console.log("Blockchain validation successful.");
  return true;
};

export { addAccident, getAllAccidents, getAccidentsBeforeDate, addAccidentFromURL, getAccidentsAfterDate };