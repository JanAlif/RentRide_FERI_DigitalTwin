import React from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { useGetCarsQuery } from '../slices/carsApiSlice';
import carImage from '../assets/car.png'; // Import car image

const containerStyle = {
  width: '100%',
  height: '500px',
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

const CarsMap = ({ onCarSelect }) => {
  const { data: cars, error, isLoading } = useGetCarsQuery();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading cars</div>;

  return (
    <MapContainer style={containerStyle} center={[center.lat, center.lng]} zoom={10}>
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />
      {cars.map((car) => (
        <Marker
          key={car._id}
          position={[car.location.coordinates[1], car.location.coordinates[0]]}
          icon={carIcon}
          eventHandlers={{
            click: () => onCarSelect(car._id),
          }}
        >
          <Popup>
            <div>
              <h3>{car.brand} {car.model}</h3>
              <p>{car.inUse ? "In Use" : "Available"}</p>
              <button onClick={() => onCarSelect(car._id)}>Select Car</button>
            </div>
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
};

export default CarsMap;
