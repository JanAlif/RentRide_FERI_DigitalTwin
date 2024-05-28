import {
  Card,
  CardBody,
  CardFooter,
  Typography,
  Button,
} from "@material-tailwind/react";

export function CarCard({ car }) {
  return (
    <Card className="mt-6 w-96">
      <CardBody>
        <Typography variant="h5" color="blue-gray" className="mb-2">
          {car.brand} {car.model}
        </Typography>
        <Typography>Year: {car.year}</Typography>
        <Typography>Kilometers: {car.totalKm}</Typography>
        <div className="flex">
          {!car.isAvailable ? (
            <>
              <div className="w-4 h-4 bg-green-500 rounded-full ml-1 mt-1"></div>
              <Typography className="ml-1">Available</Typography>
            </>
          ) : (
            <>
              <div className="w-4 h-4 bg-red-500 rounded-full"></div>
              <Typography className="ml-1">Not available</Typography>
            </>
          )}
        </div>
      </CardBody>
      <CardFooter className="pt-0">
        <Button>Rent it!</Button>
      </CardFooter>
    </Card>
  );
}
