import React from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import { useGetCarsQuery } from "../slices/carsApiSlice";
import carImage from "../assets/car.png";
import { useNavigate, Link } from "react-router-dom";

const containerStyle = {
  width: "100%",
  height: "500px",
};

const center = {
  lat: 46.55913376337574,
  lng: 15.638080093732981,
};

const carIcon = new L.Icon({
  iconUrl: carImage,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

const CarsMap = () => {
  const { data: cars, error, isLoading } = useGetCarsQuery();
  const navigate = useNavigate();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading cars</div>;

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
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />
      {cars.map((car) => (
        <Marker
          key={car._id}
          position={[car.location.coordinates[1], car.location.coordinates[0]]}
          icon={carIcon}
        >
          <Popup>
            <div className="flex flex-col">
              <h3 className="mb-1 font-bold">
                {car.brand} {car.model}
              </h3>
              <h3 className="mb-2">{car.inUse ? "In Use" : "Available"}</h3>

              <Link
                to={`/car/${car._id}`}
                className="mb-2 bg-blue-500 text-white px-4 py-2 rounded text-center"
                style={{ color: "white" }}
              >
                View Details
              </Link>
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
