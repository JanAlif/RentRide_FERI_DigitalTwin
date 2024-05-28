import React from "react";
import { useParams } from "react-router-dom";
import { useGetCarQuery } from "../slices/carsApiSlice";

export function CarDetailScreen(){
  const { id } = useParams();
  const { data: car, error, isLoading } = useGetCarQuery(id);

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading car details</div>;

  return (
    <div>
      <h1>{car.brand} {car.model}</h1>
      <p>Year: {car.year}</p>
      <p>Total Km: {car.totalKm}</p>
      <p>{car.isElectric ? "Electric" : "Non-Electric"}</p>
      <img src={car.carpic} alt={`${car.brand} ${car.model}`} />
      <p>{car.inUse ? "Currently in use" : "Available"}</p>
    </div>
  );
};


