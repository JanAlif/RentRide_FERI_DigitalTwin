import asyncHandler from "express-async-handler";
import RideModel from "../models/rideModel.js";
import UserModel from "../models/userModel.js";
import CarModel from "../models/carModel.js";

// adds a ride to the DB
// POST /api/rides
const addRide = asyncHandler(async (req, res) => {
  console.log("Received request to add a ride");
  const { driverId, carId, startLocation, endLocation, path, startTime, endTime, status, distance } = req.body;

  try {
    const car = await CarModel.findById(carId);
    const user = await UserModel.findById(driverId);

    if (!car) {
      return res.status(404).json({ message: "Car not found" });
    }
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const startLocationParsed = JSON.parse(startLocation).coordinates;
    const endLocationParsed = JSON.parse(endLocation).coordinates;

    // Calculate cost (credits are equal to distance rounded to an integer)
    const rideCost = Math.ceil(distance);

    if (user.credit < rideCost) {
      return res.status(400).json({ message: "Insufficient credits for this ride." });
    }

    // Validate the blockchain
    const isBlockchainValid = await validateBlockchain();
    if (!isBlockchainValid) {
      return res.status(500).json({ message: "Blockchain validation failed. Ride cannot be added." });
    }

    // Blockchain logic: Get the last blockchain-eligible ride
    let previousHash = null;
    if (rideCost) {
      const lastRide = await RideModel.findOne({ cost: { $exists: true } }).sort({ createdAt: -1 });
      previousHash = lastRide ? lastRide.currentHash : null;
    }

    const ride = new RideModel({
      driver: user._id,
      car: car._id,
      startLocation: { type: "Point", coordinates: startLocationParsed },
      endLocation: { type: "Point", coordinates: endLocationParsed },
      path,
      startTime,
      endTime,
      status: status || "pending",
      cost: rideCost,
      previousHash, // Set the previous hash for blockchain-eligible rides
    });

    await ride.save();

    // Deduct credits from user
    user.credit -= rideCost;
    await user.save();

    // Update car's previous rides
    car.previousRides.push(ride._id);
    await car.save();

    res.status(201).json(ride);
  } catch (error) {
    console.error("Error adding ride:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
});

//retrieves ride by ID
//GET /api/rides/:id
const getRideById = asyncHandler(async (req, res) => {
  const ride = await RideModel.findById(req.params.id);
  if (ride) {
    res.status(200).json(ride);
  } else {
    res.status(404);
    throw new Error("Ride not found");
  }
});

//retrieves all rides
//GET /api/rides
const getAllRides = asyncHandler(async (req, res) => {
  const rides = await RideModel.find({}).populate("driver").populate("car");
  if (rides && rides.length > 0) {
    res.status(200).json(rides);
  } else {
    res.status(404).json({ message: "No rides found" });
  }
});

//updates ride status
//PUT /api/rides/:id
const updateRide = asyncHandler(async (req, res) => {
  const ride = await RideModel.findById(req.params.id);
  if (ride) {
    ride.status = req.body.status || ride.status;
    ride.endTime = req.body.endTime || ride.endTime;
    if (req.body.endLocation) {
      ride.endLocation = JSON.parse(req.body.endLocation);
    }
    if (req.body.startLocation) {
      ride.startLocation = JSON.parse(req.body.startLocation);
    }
    const updatedRide = await ride.save();
    res.status(200).json(updatedRide);
  } else {
    res.status(404).json({ message: "Ride not found" });
  }
});

//DELETE /api/rides/:id
const deleteRide = asyncHandler(async (req, res) => {
  const ride = await RideModel.findByIdAndDelete(req.params.id);
  if (ride) {
    res.status(200).json({ message: "Ride removed" });
  } else {
    res.status(404);
    throw new Error("Ride not found");
  }
});

//util functions
const coordinatesWithPoint = (coordinates) => {
  return {
    type: "Point",
    coordinates,
  };
};

const validateBlockchain = async () => {
  const rides = await RideModel.find({ cost: { $exists: true } }).sort({ createdAt: 1 });

  for (let i = 1; i < rides.length; i++) {
    const previousRide = rides[i - 1];
    const currentRide = rides[i];

    if (!currentRide.previousHash || !currentRide.currentHash) {
      console.warn(`Skipping non-compliant ride ${currentRide._id}`);
      continue;
    }

    // Check if the `previousHash` matches the `currentHash` of the previous block
    if (currentRide.previousHash !== previousRide.currentHash) {
      console.error(`Blockchain broken between rides ${previousRide._id} and ${currentRide._id}`);
      return false;
    }

    // Check if the `currentHash` is valid
    if (currentRide.currentHash !== currentRide.generateHash()) {
      console.error(`Invalid hash for ride ${currentRide._id}`);
      return false;
    }
  }

  return true;
};

export { addRide, getRideById, getAllRides, updateRide, deleteRide };
