import React from "react";
import { useParams } from "react-router-dom";
import { useGetCarQuery } from "../slices/carsApiSlice";
import { useGetAllRidesQuery } from "../slices/usersApiSlice";

export function CarDetailScreen() {
  const { id } = useParams();
  const { data: car, error: carError, isLoading: carLoading } = useGetCarQuery(id);
  const { data: rides, error: ridesError, isLoading: ridesLoading } = useGetAllRidesQuery();

  if (carLoading || ridesLoading) {
    return <div className="flex justify-center items-center h-full"><div className="loader"></div></div>;
  }

  if (carError || ridesError) {
    return <div className="text-red-500 text-center">Error loading car details or rides</div>;
  }

  const carRides = rides.filter(ride => ride.car && ride.car._id === id);

  return (
    <div className="max-w-screen-lg mx-auto p-4">
      <div className="bg-white shadow rounded-lg p-6 mb-4">
        <h2 className="text-2xl font-bold text-center mb-4">
          {car.brand} {car.model}
        </h2>
        <hr className="my-4" />
        <h3 className="text-xl font-semibold mb-2">Details:</h3>
        <ul className="list-none">
          <li className="py-2">
            <strong>Year:</strong> {car.year}
          </li>
          <li className="py-2">
            <strong>Total Km:</strong> {Math.trunc(car.totalKm)}
          </li>
          <li className="py-2">
            <strong>Type:</strong> {car.isElectric ? "Electric" : "Non-Electric"}
          </li>
          <li className="py-2">
            <strong>Status:</strong> {car.inUse ? "Currently in use" : "Available"}
          </li>
        </ul>
      </div>

      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-xl font-semibold mb-4">Previous Rides:</h3>
        {carRides && carRides.length > 0 ? (
          <ul className="list-none">
            {carRides.map((ride) => (
              <li key={ride._id} className="py-2">
                <div>
                  <strong>Ride started on:</strong> {new Date(ride.startTime).toLocaleString()}
                </div>
                {ride.endTime && (
                  <div>
                    <strong>Duration:</strong> {((new Date(ride.endTime) - new Date(ride.startTime)) / 1000 / 60).toFixed(2)} minutes
                  </div>
                )}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-500 text-center">No previous rides available.</p>
        )}
      </div>
    </div>
  );
};

export default CarDetailScreen;
