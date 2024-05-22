import asyncHandler from "express-async-handler";
import CarModel from "../models/carModel.js";
import ChargePointModel from "../models/chargePointModel.js";

//retrieve all cars
//GET /api/cars
const getAllCars = asyncHandler(async (req, res) => {
  const cars = await CarModel.find({});
  if (cars && cars.length > 0) {
    res.status(200).json(cars);
  } else {
    res.status(404).json({ message: "No cars found" });
    throw new Error("Car not found");
  }
});

//retrieve car by ID
//GET /api/cars/:id
const getCarById = asyncHandler(async (req, res) => {
  const car = await CarModel.findById(req.params.id);
  if (car) {
    res.status(200).json(car);
  } else {
    res.status(404).json({ message: "No cars found" });
    throw new Error("Car not found");
  }
});

//adds a car to the DB
//POST /api/cars
const addCar = asyncHandler(async (req, res) => {
  const { brand, model, year, totalKm, isElectric, location } = req.body;
  const car = await CarModel.create({
    brand,
    model,
    year,
    totalKm,
    isElectric,
    location: coordinatesWithPoint(JSON.parse(location)),
  });
  if (car) {
    res.status(201).json(car);
  } else {
    res.status(400);
    throw new Error("Invalid car data");
  }
});

//updates car in DB
//PUT /api/cars/:id
const updateCar = asyncHandler(async (req, res) => {
  const car = await CarModel.findById(req.params.id);
  if (car) {
    car.brand = req.body.brand || car.brand;
    car.model = req.body.model || car.model;
    car.year = req.body.year || car.year;
    car.totalKm = req.body.totalKm || car.totalKm;
    car.isElectric = req.body.isElectric || car.isElectric;
    if (req.body.longitude && req.body.latitude) {
      car.location = {
        type: "Point",
        coordinates: [req.body.longitude, req.body.latitude],
      };
    } else {
      car.location = car.location;
    }
    const updatedCar = await car.save();
    res.status(200).json(updatedCar);
  } else {
    res.status(404);
    throw new Error("Car not found");
  }
});

//removes a car from the DB
//DELETE /api/cars/:id
const deleteCar = asyncHandler(async (req, res) => {
  const car = await CarModel.findByIdAndDelete(req.params.id);
  if (car) {
    res.status(200).json({ message: "Car removed" });
  } else {
    res.status(404);
    throw new Error("Car not found");
  }
});

//retrieve charge points nearby
//GET /api/cars/chargepointsnearby
const getChargePointsNearby = asyncHandler(async (req, res) => {
  const car = await CarModel.findById(req.body.carId);

  if (!car) {
    res.status(404);
    throw new Error("Car not found");
  }

  const chargePoints = await ChargePointModel.find({
    location: {
      $geoWithin: {
        $centerSphere: [car.location.coordinates, 10 / 6371],
      },
    },
  });

  res.status(200).json(chargePoints);
});

//util functions
const coordinatesWithPoint = (coordinates) => {
  return {
    type: "Point",
    coordinates,
  };
};

export {
  getAllCars,
  getCarById,
  addCar,
  updateCar,
  deleteCar,
  getChargePointsNearby,
};
