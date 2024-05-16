import asyncHandler from "express-async-handler";
import RideModel from "../models/rideModel.js";
import UserModel from "../models/userModel.js";
import CarModel from "../models/carModel.js";

//adds a ride to the DB
//POST /api/rides
const addRide = asyncHandler(async (req, res) => {
  const { carId, startLocation, endLocation } = req.body;
  const car = await CarModel.findById(carId);
  const user = req.user;

  if (!car) {
    res.status(404);
    throw new Error("Car not found");
  }
  if (!user) {
    res.status(404);
    throw new Error("User not found");
  }

  const ride = await RideModel.create({
    driver: user._id,
    car: car._id,
    startLocation: coordinatesWithPoint(JSON.parse(startLocation)),
    endLocation: coordinatesWithPoint(JSON.parse(endLocation)),
    path: {
      type: "LineString",
      coordinates: [JSON.parse(startLocation), JSON.parse(endLocation)],
    },
  });

  if (ride) {
    res.status(201).json(ride);
  } else {
    res.status(400);
    throw new Error("Invalid ride data");
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
    ride.endLocation =
      coordinatesWithPoint(req.body.endLocation) || ride.endLocation;
    const updatedRide = await ride.save();
    res.status(200).json(updatedRide);
  } else {
    res.status(404);
    throw new Error("Ride not found");
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

export { addRide, getRideById, getAllRides, updateRide, deleteRide };
