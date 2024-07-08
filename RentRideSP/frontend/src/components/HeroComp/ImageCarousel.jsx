import { Carousel, Typography, Button } from "@material-tailwind/react";

import CarImg from "../../assets/carImage.jpg";
import mapImg from "../../assets/googleMaps.png";

export function CarouselWithContent() {
  return (
    <Carousel className="rounded-xl">
      <div className="relative h-full w-full">
        <img
          src={CarImg}
          alt="image 1"
          className="h-full w-full object-cover"
        />
        <div className="absolute inset-0 grid h-full w-full place-items-center bg-black/25">
          <div className="w-3/4 text-center md:w-2/4">
            <Typography
              variant="h1"
              color="white"
              className="mb-4 text-3xl md:text-4xl lg:text-5xl"
            >
              Discover Our Luxurious Car Collection
            </Typography>
            <Typography
              variant="lead"
              color="white"
              className="mb-12 opacity-80"
            >
              Indulge in the ultimate driving experience with our exclusive
              range of luxury vehicles. From high-performance sports cars to
              refined sedans, enjoy unparalleled comfort and style. Book your
              luxury car today and drive in ultimate sophistication.
            </Typography>
          </div>
        </div>
      </div>
      <div className="relative h-full w-full">
        <img
          src={mapImg}
          alt="image 2"
          className="h-full w-full object-cover"
        />
        <div className="absolute inset-0 grid h-full w-full items-center bg-black/75">
          <div className="w-3/4 pl-12 md:w-2/4 md:pl-20 lg:pl-32">
            <Typography
              variant="h1"
              color="white"
              className="mb-4 text-3xl md:text-4xl lg:text-5xl"
            >
              Easy Car Drop-Off at Any Location
            </Typography>
            <Typography
              variant="lead"
              color="white"
              className="mb-12 opacity-80"
            >
              Travel to your desired city with the freedom and flexibility you
              deserve. Whether you're on a business trip, a family vacation, or
              a spontaneous weekend getaway, our extensive network of garages
              across the country ensures that you can return your car at the
              location that is most convenient for you.
            </Typography>
            <div className="flex gap-2 justify-center">
              <Button size="lg" color="white">
                Show cars
              </Button>
            </div>
          </div>
        </div>
      </div>
    </Carousel>
  );
}
