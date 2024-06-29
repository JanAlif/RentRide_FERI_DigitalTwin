import React, { useEffect } from "react";
import {
  Card,
  CardBody,
  CardFooter,
  Typography,
  Button,
  Checkbox,
  Input,
} from "@material-tailwind/react";

import DropDown from "../components/carScreenComp/DropDown";

import { useState } from "react";
import { CarCard } from "../components/CarCard";
import { useGetCarsQuery } from "../slices/carsApiSlice";

function CarScreen() {
  const { data: cars, error, isLoading } = useGetCarsQuery();
  const [isElectric, setIsElectric] = useState(false);
  const [notElectric, setNotElectric] = useState(false);
  const [filteredCars, setFilteredCars] = useState([]);
  const [searchBrand, setSearchBrand] = useState("");
  const [brands, setBrands] = useState([]);
  const [yearFilter, setYearFilter] = useState("");

  useEffect(() => {
    if (cars) {
      setFilteredCars(cars);
      const carBrands = cars.map((car) => car.brand);
      setBrands([...new Set(carBrands)]);
    }
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

    if (searchBrand && brands.includes(searchBrand)) {
      updatedCars = cars.filter((car) => car.brand === searchBrand);
    }
    setFilteredCars(updatedCars);
  };

  return (
    <>
      <Card className="mt-6 w-full">
        <CardBody className="flex-col">
          <Typography variant="h5" color="blue-gray" className="mb-2">
            Filters:
          </Typography>
          <div className="flex">
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
              <Input
                variant="outlined"
                label="Brand"
                placeholder="Brand"
                className="w-64"
                onChange={(e) => setSearchBrand(e.target.value)}
              />
              <CardFooter className="flex ml-5 pl-0">
                <Button type="submit">Search</Button>
              </CardFooter>
            </form>
          </div>
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
