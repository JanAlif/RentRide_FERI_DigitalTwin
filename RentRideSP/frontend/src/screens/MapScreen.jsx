import React, { useState, useEffect, useRef } from 'react';
import { format } from 'date-fns';
import { Card, Input, Button, Typography } from '@material-tailwind/react';
import { GoogleMap, LoadScript, DirectionsRenderer } from '@react-google-maps/api';
import Map from '../components/Map'; // Import the Map component

const containerStyle = {
  width: '100%',
  height: '400px',
};

export function MapScreen() {
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [departureTime, setDepartureTime] = useState('');
  const [trafficModel, setTrafficModel] = useState('best_guess');
  const [directionsResponse, setDirectionsResponse] = useState(null);
  const [error, setError] = useState('');
  const [arrivalTime, setArrivalTime] = useState('');
  const [originSuggestions, setOriginSuggestions] = useState([]);
  const [destinationSuggestions, setDestinationSuggestions] = useState([]);
  const originAutocompleteService = useRef(null);
  const destinationAutocompleteService = useRef(null);

  const [avoidTolls, setAvoidTolls] = useState(false);
  const [avoidHighways, setAvoidHighways] = useState(false);
  const [avoidFerries, setAvoidFerries] = useState(false);

  useEffect(() => {
    if (window.google) {
      originAutocompleteService.current = new window.google.maps.places.AutocompleteService();
      destinationAutocompleteService.current = new window.google.maps.places.AutocompleteService();
    }
  }, []);

  const handleOriginChange = (e) => {
    const value = e.target.value;
    setOrigin(value);
    // Uncomment the following lines to enable autocomplete API call
    // if (value.length >= 3) {
    //   originAutocompleteService.current.getPlacePredictions({ input: value }, (predictions, status) => {
    //     if (status === window.google.maps.places.PlacesServiceStatus.OK) {
    //       setOriginSuggestions(predictions);
    //     }
    //   });
    // } else {
    //   setOriginSuggestions([]);
    // }
  };

  const handleDestinationChange = (e) => {
    const value = e.target.value;
    setDestination(value);
    // Uncomment the following lines to enable autocomplete API call
    // if (value.length > 3) {
    //   destinationAutocompleteService.current.getPlacePredictions({ input: value }, (predictions, status) => {
    //     if (status === window.google.maps.places.PlacesServiceStatus.OK) {
    //       setDestinationSuggestions(predictions);
    //     }
    //   });
    // } else {
    //   setDestinationSuggestions([]);
    // }
  };

  const handleDirections = () => {
    if (!origin || !destination) {
      setError('Both origin and destination are required.');
      return;
    }
    setError('');

    const directionsService = new window.google.maps.DirectionsService();
    const request = {
      origin,
      destination,
      travelMode: window.google.maps.TravelMode.DRIVING,
      avoidTolls,
      avoidHighways,
      avoidFerries,
      drivingOptions: {
        departureTime: departureTime ? new Date(departureTime) : new Date(),
        trafficModel: trafficModel,
      }
    };

    directionsService.route(request, (result, status) => {
      if (status === window.google.maps.DirectionsStatus.OK) {
        setDirectionsResponse(result);
        const leg = result.routes[0].legs[0];
        const arrival = new Date(new Date(departureTime).getTime() + leg.duration_in_traffic.value * 1000);
        setArrivalTime(arrival);
      } else {
        console.error(`Error fetching directions ${result}`);
        setError('Failed to get directions.');
      }
    });
  };

  const handleOriginSelect = (description) => {
    setOrigin(description);
    setOriginSuggestions([]);
  };

  const handleDestinationSelect = (description) => {
    setDestination(description);
    setDestinationSuggestions([]);
  };

  return (
    <LoadScript googleMapsApiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY} libraries={['places', 'marker']}>
      <Card color="transparent" shadow={false} className="p-6">
        <Typography variant="h4" color="blue-gray" className="text-center">
          Route
        </Typography>
        <div className="flex flex-col gap-6 mt-6">
          <Input
            size="lg"
            placeholder="Origin..."
            value={origin}
            onChange={handleOriginChange}
          />
          <Input
            size="lg"
            placeholder="Destination..."
            value={destination}
            onChange={handleDestinationChange}
          />
          <div className="flex gap-4">
            <label>
              <input
                type="checkbox"
                checked={avoidTolls}
                onChange={(e) => setAvoidTolls(e.target.checked)}
              />
              Avoid Tolls
            </label>
            <label>
              <input
                type="checkbox"
                checked={avoidHighways}
                onChange={(e) => setAvoidHighways(e.target.checked)}
              />
              Avoid Highways
            </label>
            <label>
              <input
                type="checkbox"
                checked={avoidFerries}
                onChange={(e) => setAvoidFerries(e.target.checked)}
              />
              Avoid Ferries
            </label>
          </div>
          <div className="flex gap-4">
            <Input
              type="datetime-local"
              label="Departure Time"
              value={departureTime}
              onChange={(e) => setDepartureTime(e.target.value)}
            />
          </div>
          <div className="flex gap-4">
            <label>
              <input
                type="radio"
                name="trafficModel"
                value="best_guess"
                checked={trafficModel === 'best_guess'}
                onChange={(e) => setTrafficModel(e.target.value)}
              />
              Best Guess
            </label>
            <label>
              <input
                type="radio"
                name="trafficModel"
                value="pessimistic"
                checked={trafficModel === 'pessimistic'}
                onChange={(e) => setTrafficModel(e.target.value)}
              />
              Pessimistic
            </label>
            <label>
              <input
                type="radio"
                name="trafficModel"
                value="optimistic"
                checked={trafficModel === 'optimistic'}
                onChange={(e) => setTrafficModel(e.target.value)}
              />
              Optimistic
            </label>
          </div>
          <Button className="mt-6" onClick={handleDirections}>
            Draw Route
          </Button>
          {arrivalTime && (
            <Typography color="gray" className="mt-4 text-center">
              <strong>Estimated Arrival Time:</strong> {format(arrivalTime, "yyyy-MM-dd'T'HH:mm")}
            </Typography>
          )}
          {error && <Typography color="red" className="mt-2">{error}</Typography>}
        </div>
        <Map directionsResponse={directionsResponse} />
      </Card>
    </LoadScript>
  );
}
