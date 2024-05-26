import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, Polyline, Marker } from 'react-leaflet';
import L from 'leaflet';
import startImage from '../assets/start.png';
import finishImage from '../assets/finish.png';
import carImage from '../assets/car.png'; // Import car image

const containerStyle = {
  width: '100%',
  height: '500px',
};

const center = {
  lat: 46.55913376337574,
  lng: 15.638080093732981,
};

const startIcon = new L.Icon({
  iconUrl: startImage,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

const endIcon = new L.Icon({
  iconUrl: finishImage,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

const carIcon = new L.Icon({
  iconUrl: carImage,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

const parseDirectionsResponse = (response) => {
  if (!response) return [];
  const legs = response.routes[0].legs;
  const route = [];

  legs.forEach((leg) => {
    leg.steps.forEach((step) => {
      const nextSegment = step.path.map((point) => ({
        lat: point.lat(),
        lng: point.lng(),
      }));
      route.push(...nextSegment);
    });
  });

  return route;
};

const Map = ({ directionsResponse, departureTime, arrivalTime, speedFactor = 1, isPlaying, isStopped, setProgress }) => {
  const [route, setRoute] = useState([]);
  const [startPoint, setStartPoint] = useState(null);
  const [endPoint, setEndPoint] = useState(null);
  const [carPosition, setCarPosition] = useState(null);
  const animationRef = useRef(null);
  const animationStartTimeRef = useRef(null);
  const progressRef = useRef(0);
  const pauseTimeRef = useRef(null);

  useEffect(() => {
    if (directionsResponse) {
      const parsedRoute = parseDirectionsResponse(directionsResponse);
      setRoute(parsedRoute);
      setStartPoint(parsedRoute[0]);
      setEndPoint(parsedRoute[parsedRoute.length - 1]);
      setCarPosition(parsedRoute[0]);
      progressRef.current = 0;
      setProgress(0);
    }
  }, [directionsResponse, setProgress]);

  useEffect(() => {
    if (!route.length || !startPoint || !endPoint || !departureTime || !arrivalTime) return;

    const startTimestamp = new Date(departureTime).getTime();
    const endTimestamp = new Date(arrivalTime).getTime();
    let duration = (endTimestamp - startTimestamp) / speedFactor;

    const animateCar = () => {
      const currentTime = new Date().getTime();
      const elapsedTime = currentTime - animationStartTimeRef.current;
      const t = Math.min(1, elapsedTime / duration); // Calculate the time progress (0 to 1)

      const index = Math.floor(t * (route.length - 1));
      setCarPosition(route[index]);
      progressRef.current = t * 100;
      setProgress(progressRef.current);

      if (t < 1) {
        animationRef.current = requestAnimationFrame(animateCar);
      }
    };

    if (isPlaying && !isStopped) {
      if (animationStartTimeRef.current === null) {
        animationStartTimeRef.current = new Date().getTime();
      } else if (pauseTimeRef.current) {
        // Adjust animation start time to resume from pause point
        animationStartTimeRef.current += new Date().getTime() - pauseTimeRef.current;
        pauseTimeRef.current = null;
      }
      animationRef.current = requestAnimationFrame(animateCar);
    } else if (!isPlaying && animationRef.current) {
      cancelAnimationFrame(animationRef.current);
      pauseTimeRef.current = new Date().getTime(); // Capture the pause time
    }

    if (isStopped) {
      setCarPosition(startPoint);
      cancelAnimationFrame(animationRef.current);
      animationStartTimeRef.current = null;
      progressRef.current = 0;
      setProgress(0);
    }

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [route, startPoint, endPoint, departureTime, arrivalTime, speedFactor, isPlaying, isStopped, setProgress]);

  return (
    <MapContainer style={containerStyle} center={[center.lat, center.lng]} zoom={10}>
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />
      {route.length > 0 && (
        <>
          <Polyline positions={route} color="blue" />
          {startPoint && <Marker position={startPoint} icon={startIcon} />}
          {endPoint && <Marker position={endPoint} icon={endIcon} />}
          {carPosition && <Marker position={carPosition} icon={carIcon} />}
        </>
      )}
    </MapContainer>
  );
};

export default Map;
