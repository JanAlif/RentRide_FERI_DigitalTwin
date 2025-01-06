// backend/models/Traffic.js

import mongoose from 'mongoose';

const coordinateSchema = mongoose.Schema({
    latitude: { type: Number, required: true },
    longitude: { type: Number, required: true },
});

const trafficSchema = mongoose.Schema({
    imageUrl: { type: String, required: true, unique: true },
    detectedVehicles: { type: Number, required: true },
    classificationResult: { type: Number, required: true }, // 1, 2, or 3
    coordinates: { type: [coordinateSchema], default: [] }, // Array of coordinates
    timestamp: { type: Date, default: Date.now },
});

// Create an index on imageUrl to enforce uniqueness at the database level
trafficSchema.index({ imageUrl: 1 }, { unique: true });

const Traffic = mongoose.model('Traffic', trafficSchema);

export default Traffic;