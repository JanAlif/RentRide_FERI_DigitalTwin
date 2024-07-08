import React from "react";
import { Card, Typography, Input, Button } from "@material-tailwind/react";
import CarsMap from "../components/CarsMap";
import { useNavigate } from "react-router-dom";
import { CarouselWithContent } from "../components/HeroComp/ImageCarousel";

function HeroScreen() {
  const navigate = useNavigate();

  const handleCarSelect = (carId) => {
    navigate(`/map?carId=${carId}`);
  };

  return (
    <>
      <header className="">
        <div className="grid min-h-[82vh] w-full lg:h-[54rem] md:h-[34rem] place-items-stretch bg-[url('/image/bg-hero-17.svg')] bg-center bg-contain bg-no-repeat">
          <div className="container mx-auto px-4 text-center">
            <Typography
              variant="h1"
              color="blue-gray"
              className="mx-auto my-6 w-full leading-snug  !text-2xl lg:max-w-3xl lg:!text-5xl"
            >
              Unlock the{" "}
              <span className="text-green-500 leading-snug ">Road Ahead</span>{" "}
              with Our Premier{" "}
              <span className="leading-snug text-green-500">Car Rentals</span>.
            </Typography>
            <Typography
              variant="lead"
              className="mx-auto w-full !text-gray-500 lg:text-lg text-base"
            >
              Where Every Mile is a Memory – Start Your Journey with Us
            </Typography>
            <div className="w-full pt-10">
              <CarouselWithContent />
            </div>
            <Typography
              className="mt-16 leading-snug"
              color="blue-gray"
              variant="h1"
            >
              Rent the Perfect Ride Near You!
            </Typography>
            <div className=" mt-2 mb-10 border-4 border-black rounded-md">
              <CarsMap onCarSelect={handleCarSelect} />
            </div>
          </div>
          <div>
            <hr className="my-8 border-blue-gray-50" />
            <Typography
              color="blue-gray"
              className="text-center font-normal pb-4"
            >
              &copy; 2024 RentRide
            </Typography>
          </div>
        </div>
      </header>
    </>
  );
}

export default HeroScreen;
