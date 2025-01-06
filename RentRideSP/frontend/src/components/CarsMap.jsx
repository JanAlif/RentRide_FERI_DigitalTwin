// frontend/src/components/CarsMap.js

import React from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline } from "react-leaflet";
import L from "leaflet";
import { useGetCarsQuery } from "../slices/carsApiSlice";
import { useGetTrafficsQuery } from "../slices/trafficApiSlice"; // Import Traffic Query Hook
import carImage from "../assets/car.png";
import { useNavigate, Link } from "react-router-dom";

// Styles for the map container
const containerStyle = {
  width: "100%",
  height: "500px",
};

// Initial center position of the map
const center = {
  lat: 46.55913376337574,
  lng: 15.638080093732981,
};

// Custom icon for car markers
const carIcon = new L.Icon({
  iconUrl: carImage,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

// Helper function to determine line color based on classificationResult
const getLineColor = (classificationResult) => {
  switch (classificationResult) {
    case 1:
      return "green";
    case 2:
      return "yellow";
    case 3:
      return "red";
    default:
      return "blue"; // Default color if classificationResult is unknown
  }
};

const CarsMap = () => {
  // Fetch car data using RTK Query
  const {
    data: cars,
    error: carsError,
    isLoading: carsLoading,
  } = useGetCarsQuery();

  // Fetch traffic line data using RTK Query
  const {
    data: traffics,
    error: trafficsError,
    isLoading: trafficsLoading,
  } = useGetTrafficsQuery();

  const navigate = useNavigate();

  // Handle loading states
  if (carsLoading || trafficsLoading) return <div>Loading...</div>;

  // Handle error states with improved messages
  if (carsError)
    return (
      <div>
        <h2>Error Loading Cars</h2>
        <pre>{JSON.stringify(carsError, null, 2)}</pre>
      </div>
    );
  if (trafficsError)
    return (
      <div>
        <h2>Error Loading Traffic Lines</h2>
        <pre>{JSON.stringify(trafficsError, null, 2)}</pre>
      </div>
    );

  // Navigation handlers
  const handleCarDetail = (carId) => {
    navigate(`/car/${carId}`);
  };

  const handleReserveRide = (carId) => {
    navigate(`/map?carId=${carId}`);
  };

  return (
    <MapContainer
      style={containerStyle}
      center={[center.lat, center.lng]}
      zoom={10}
    >
      {/* OpenStreetMap Tile Layer */}
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />

      {/* Render traffic lines */}
      {traffics &&
        traffics.map((traffic) => (
          <Polyline
            key={traffic._id}
            positions={traffic.coordinates.map((coord) => [
              coord.latitude,
              coord.longitude,
            ])}
            color={getLineColor(traffic.classificationResult)}
            weight={5}
          />
        ))}

      {/* Render car markers */}
      {cars &&
        cars.map((car) => (
          <Marker
            key={car._id}
            position={[
              car.location.coordinates[1],
              car.location.coordinates[0],
            ]}
            icon={carIcon}
          >
            <Popup>
              <div className="flex flex-col">
                <h3 className="mb-1 font-bold">
                  {car.brand} {car.model}
                </h3>
                <h3 className="mb-2">
                  {car.inUse ? "In Use" : "Available"}
                </h3>

                {/* Link to view car details */}
                <Link
                  to={`/car/${car._id}`}
                  className="mb-2 bg-blue-500 text-white px-4 py-2 rounded text-center"
                  style={{ color: "white" }}
                >
                  View Details
                </Link>

                {/* Link to reserve a ride */}
                <Link
                  to={`/map?carId=${car._id}`}
                  className="bg-blue-500 text-white px-4 py-2 rounded text-center"
                  style={{ color: "white" }}
                >
                  Reserve Ride
                </Link>
              </div>
            </Popup>
          </Marker>
        ))}
    </MapContainer>
  );
};

export default CarsMap;