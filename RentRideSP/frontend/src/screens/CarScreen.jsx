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

import CarImage from "../assets/carScreenFilt.jpg";

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

    let updatedCars = [...cars];

    if (isElectric) {
      updatedCars = updatedCars.filter((car) => car.isElectric);
    }

    if (notElectric) {
      updatedCars = updatedCars.filter((car) => !car.isElectric);
    }

    if (
      searchBrand &&
      searchBrand.trim() !== "" &&
      brands.includes(searchBrand)
    ) {
      updatedCars = updatedCars.filter((car) => car.brand === searchBrand);
    }

    if (yearFilter && yearFilter > 1000) {
      updatedCars = updatedCars.filter((car) => car.year >= yearFilter);
    }

    setFilteredCars(updatedCars);
  };

  return (
    <>
      <Card className="mt-6">
        <CardBody className="flex-col">
          <Typography variant="h5" color="blue-gray" className="mb-2">
            Filters:
          </Typography>
          <div className="flex flex-wrap gap-2">
            <form onSubmit={submitHandler} className="">
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

              <div className="w-full mt-4">
                <Input
                  variant="outlined"
                  type="number"
                  label="Year"
                  placeholder="Year"
                  value={yearFilter}
                  className="w-full"
                  onChange={(e) => setYearFilter(e.target.value)}
                />
              </div>
              <CardFooter className="flex ml-5 pl-0">
                <Button type="submit">Search</Button>
              </CardFooter>
            </form>
            <div
              className="
flex-grow border-black border-2"
            ></div>
          </div>
        </CardBody>
      </Card>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
        {filteredCars.map((car) => (
          <CarCard key={car._id} car={car} />
        ))}
      </div>
    </>
  );
}

export default CarScreen;
