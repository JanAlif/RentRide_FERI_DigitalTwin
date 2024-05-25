import React, { useState, useEffect, useRef } from 'react';
import { format } from 'date-fns';
import { Card, Input, Button, Typography } from '@material-tailwind/react';
import Map from '../components/Map'; // Import the Leaflet Map component

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

  const [speedFactor, setSpeedFactor] = useState(1);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isStopped, setIsStopped] = useState(true);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    loadGoogleMapsScript(import.meta.env.VITE_GOOGLE_MAPS_API_KEY).then(() => {
      originAutocompleteService.current = new window.google.maps.places.AutocompleteService();
      destinationAutocompleteService.current = new window.google.maps.places.AutocompleteService();
    });
  }, []);

  const handleOriginChange = (e) => {
    const value = e.target.value;
    setOrigin(value);
  };

  const handleDestinationChange = (e) => {
    const value = e.target.value;
    setDestination(value);
  };

  const handleDirections = () => {
    if (!origin || !destination) {
      setError('Both origin and destination are required.');
      return;
    }
    setError('');

    loadGoogleMapsScript(import.meta.env.VITE_GOOGLE_MAPS_API_KEY).then(() => {
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
        },
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
    });
  };

  const handlePlay = () => {
    setIsPlaying(true);
    setIsStopped(false);
  };

  const handlePause = () => {
    setIsPlaying(false);
  };

  const handleStop = () => {
    setIsPlaying(false);
    setIsStopped(true);
    setProgress(0);
  };

  const handleSpeedChange = (e) => {
    if (!isPlaying) {
      setSpeedFactor(Number(e.target.value));
    }
  };

  return (
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
      <div className="mt-6">
        <Map
          directionsResponse={directionsResponse}
          departureTime={departureTime}
          arrivalTime={arrivalTime}
          speedFactor={speedFactor}
          isPlaying={isPlaying}
          isStopped={isStopped}
          setProgress={setProgress}
        />
        <div className="flex flex-col gap-4 mt-4 w-full">
          <div className="flex gap-4 justify-between">
            <label className="flex-1">
              Speed Factor:
              <input
                type="number"
                value={speedFactor}
                onChange={handleSpeedChange}
                min="1"
                max="100"
                disabled={isPlaying} // Disable speed change input when playing
                className="w-full"
              />
            </label>
          </div>
          <div className="flex gap-4 justify-between">
            <Button className="flex-1" onClick={handlePlay} disabled={isPlaying}>
              Play
            </Button>
            <Button className="flex-1" onClick={handlePause} disabled={!isPlaying}>
              Pause
            </Button>
            <Button className="flex-1" onClick={handleStop}>
              Stop
            </Button>
          </div>
          <div className="flex gap-4 justify-between">
            <label className="flex-1">
              Progress:
              <progress value={progress} max="100" className="w-full">{progress}%</progress>
            </label>
          </div>
        </div>
      </div>
    </Card>
  );
  
}







const loadGoogleMapsScript = (apiKey) => {
    return new Promise((resolve, reject) => {
      // Check if the script is already loaded
      if (document.querySelector(`script[src*="maps.googleapis.com/maps/api/js?key=${apiKey}"]`)) {
        resolve();
        return;
      }
      
      if (typeof window.google === 'object' && typeof window.google.maps === 'object') {
        resolve();
        return;
      }
      
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places`;
      script.async = true;
      script.defer = true;
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  };
  