import React, { useEffect } from "react";
import {
  Card,
  CardBody,
  CardFooter,
  Typography,
  Button,
  Checkbox,
} from "@material-tailwind/react";

import { useState } from "react";
import { CarCard } from "../components/CarCard";
import { useGetCarsQuery } from "../slices/carsApiSlice";

function CarScreen() {
  const { data: cars, error, isLoading } = useGetCarsQuery();
  const [isElectric, setIsElectric] = useState(false);
  const [notElectric, setNotElectric] = useState(false);
  const [filteredCars, setFilteredCars] = useState([]);

  useEffect(() => {
    if (cars) setFilteredCars(cars);
  }, [cars]);

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;

  const submitHandler = (e) => {
    e.preventDefault();
    let updatedCars = cars;

    if (isElectric && notElectric) {
      updatedCars = cars;
    } else if (isElectric) {
      updatedCars = cars.filter((car) => car.isElectric);
    } else if (notElectric) {
      updatedCars = cars.filter((car) => !car.isElectric);
    }

    setFilteredCars(updatedCars);
  };

  return (
    <>
      <Card className="mt-6 w-full">
        <CardBody>
          <Typography variant="h5" color="blue-gray" className="mb-2">
            Filters:
          </Typography>
          <form onSubmit={submitHandler}>
            <Checkbox
              label="Electric"
              checked={isElectric}
              onChange={(e) => setIsElectric(e.target.checked)}
            />
            <Checkbox
              label="Gas"
              checked={notElectric}
              onChange={(e) => setNotElectric(e.target.checked)}
            />
            <CardFooter className="pt-0">
              <Button type="submit">Search</Button>
            </CardFooter>
          </form>
          <Typography></Typography>
        </CardBody>
      </Card>
      <ul>
        {filteredCars.map((car) => (
          <li key={car._id}>
            <CarCard car={car} />
          </li>
        ))}
      </ul>
    </>
  );
}

export default CarScreen;
