import expressAsyncHandler from "express-async-handler";
import ChargePointModel from "../models/chargePointModel.js";

//adds a charge point to the DB
//POST /api/chargepoints
const addChargePoint = expressAsyncHandler(async (req, res) => {
  const {
    locationName,
    address,
    coordinates,
    connectors,
    connectorsAvailable,
  } = req.body;

  const chargePoint = await ChargePointModel.create({
    locationName,
    address,
    location: {
      type: "Point",
      coordinates: coordinates,
    },
    connectors,
    connectorsAvailable,
  });

  if (chargePoint) {
    res.status(201).json({
      message: "Charge Point added successfully",
      chargePoint: chargePoint,
    });
  } else {
    res.status(400);
    throw new Error("Invalid Charge Point data");
  }
});

//retrives all charge points from the DB
//GET /api/chargepoints
const getAllChargePoints = expressAsyncHandler(async (req, res) => {
  const chargePoints = await ChargePointModel.find({});
  if (chargePoints) {
    res.status(200).json(chargePoints);
  } else {
    res.status(404).json({ message: "No Charge Points found" });
    throw new Error("No Charge Points found");
  }
});

//retrieves a charge point by ID from the DB
//GET /api/chargepoints/:id
const getChargePointById = expressAsyncHandler(async (req, res) => {
  const chargePoint = await ChargePointModel.findById(req.params.id);
  if (chargePoint) {
    res.status(200).json(chargePoint);
  } else {
    res.status(404);
    throw new Error("Charge Point not found");
  }
});

//updates a charge point in the DB
//PUT /api/chargepoints/:id
const updateChargePoint = expressAsyncHandler(async (req, res) => {
  const chargePoint = await ChargePointModel.findById(req.params.id);

  if (chargePoint) {
    chargePoint.locationName =
      req.body.locationName || chargePoint.locationName;
    chargePoint.address = req.body.address || chargePoint.address;
    if (req.body.longitude && req.body.latitude) {
      chargePoint.location = {
        type: "Point",
        coordinates: [req.body.longitude, req.body.latitude],
      };
    } else {
      chargePoint.location = chargePoint.location;
    }
    chargePoint.connectors = req.body.connectors || chargePoint.connectors;
    chargePoint.connectorsAvailable =
      req.body.connectorsAvailable || chargePoint.connectorsAvailable;

    const updatedChargePoint = await chargePoint.save();
    res.json({
      message: "Charge Point updated successfully",
      chargePoint: updatedChargePoint,
    });
  } else {
    res.status(404);
    throw new Error("Charge Point not found");
  }
});

//removes a charge point from the DB
//DELETE /api/chargepoints/:id
const deleteChargePoint = expressAsyncHandler(async (req, res) => {
  const chargePoint = await ChargePointModel.findByIdAndDelete(req.params.id);
  if (chargePoint) {
    res.json({ message: "Charge Point removed" });
  } else {
    res.status(404);
    throw new Error("Charge Point not found");
  }
});

export {
  addChargePoint,
  getAllChargePoints,
  getChargePointById,
  updateChargePoint,
  deleteChargePoint,
};
