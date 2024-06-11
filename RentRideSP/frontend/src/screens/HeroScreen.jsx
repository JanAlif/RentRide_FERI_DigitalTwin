import React from "react";
import { Card, Typography } from "@material-tailwind/react";
import CarsMap from "../components/CarsMap";
import { useNavigate } from "react-router-dom";

function HeroScreen() {
  const navigate = useNavigate();

  const handleCarSelect = (carId) => {
    navigate(`/map?carId=${carId}`);
  };

  return (
    <>
      <Typography variant="h2" color="blue-gray" className="mb-2">
        Order your ride now!
      </Typography>
      <CarsMap
        onCarSelect={handleCarSelect}
        className="border-5 border-black"
      />
    </>
  );
}

export default HeroScreen;
