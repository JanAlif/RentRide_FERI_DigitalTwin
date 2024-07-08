import React from "react";
import {
  Dialog,
  DialogBody,
  DialogFooter,
  Button,
  Typography,
} from "@material-tailwind/react";
import { useNavigate } from "react-router-dom";

const CarModal = ({ car, isOpen, onClose }) => {
  const navigate = useNavigate();

  return (
    <Dialog open={isOpen} handler={onClose}>
      <DialogBody>
        <Typography variant="h5" color="blue-gray" className="mb-4">
          Rent {car.brand} {car.model}
        </Typography>
        <Typography>Year: {car.year}</Typography>
        <Typography>Kilometers: {car.totalKm}</Typography>
        <Typography>Available: {car.isAvailable ? "No" : "Yes"}</Typography>
      </DialogBody>
      <DialogFooter>
        <Button variant="text" color="red" onClick={onClose}>
          Close
        </Button>
        <Button
          variant="gradient"
          color="green"
          onClick={() => {
            navigate(`/map?carId=${car._id}`);
          }}
        >
          Confirm Rental
        </Button>
      </DialogFooter>
    </Dialog>
  );
};

export default CarModal;
