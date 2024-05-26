import React, { useState, useEffect } from 'react';
import { Typography, Button, Input, IconButton, Switch } from '@material-tailwind/react';
import { useUpdateCarMutation } from '../slices/adminSlice';
import { FaPencilRuler } from "react-icons/fa";
import { MdDeleteForever } from "react-icons/md";

export function CarAdminDetail({ cars, handleDeleteCar }) {
    const [editingCarId, setEditingCarId] = useState(null);
    const [updatedCar, setUpdatedCar] = useState({ brand: '', model: '', year: '', totalKm: '', isElectric: false });
    const [updateCar] = useUpdateCarMutation();
    const [localCars, setLocalCars] = useState([]);

    useEffect(() => {
        setLocalCars(cars);
    }, [cars]);

    const handleEditClick = (car) => {
        setEditingCarId(car._id);
        setUpdatedCar({
            brand: car.brand,
            model: car.model,
            year: car.year,
            totalKm: car.totalKm,
            isElectric: car.isElectric,
        });
    };

    const handleUpdateCar = async (id) => {
        try {
            const carData = {
                brand: updatedCar.brand,
                model: updatedCar.model,
                year: updatedCar.year,
                totalKm: updatedCar.totalKm,
                isElectric: updatedCar.isElectric,
            };
            await updateCar({ id, updatedCar: carData }).unwrap();
            setLocalCars(prevCars =>
                prevCars.map(car =>
                    car._id === id ? { ...car, ...carData } : car
                )
            );
            setEditingCarId(null);
        } catch (error) {
            console.error('Error updating car:', error);
        }
    };

    return (
        <ul>
            {localCars.map(car => (
                <li key={car._id} className="flex flex-col mb-2">
                    {editingCarId === car._id ? (
                        <div className="flex gap-4 mb-2">
                            <Input
                                label="Brand"
                                value={updatedCar.brand}
                                onChange={(e) => setUpdatedCar({ ...updatedCar, brand: e.target.value })}
                            />
                            <Input
                                label="Model"
                                value={updatedCar.model}
                                onChange={(e) => setUpdatedCar({ ...updatedCar, model: e.target.value })}
                            />
                            <Input
                                label="Year"
                                type="number"
                                value={updatedCar.year}
                                onChange={(e) => setUpdatedCar({ ...updatedCar, year: e.target.value })}
                            />
                            <Input
                                label="Total Km"
                                type="number"
                                value={updatedCar.totalKm}
                                onChange={(e) => setUpdatedCar({ ...updatedCar, totalKm: e.target.value })}
                            />
                            <div className="flex items-center">
                                <Typography className="mr-2">Electric</Typography>
                                <Switch
                                    checked={updatedCar.isElectric}
                                    onChange={(e) => setUpdatedCar({ ...updatedCar, isElectric: e.target.checked })}
                                />
                            </div>
                            <div className="flex justify-between">
                                <Button onClick={() => handleUpdateCar(car._id)}>Save</Button>
                                <Button color="red" onClick={() => setEditingCarId(null)}>Cancel</Button>
                            </div>
                        </div>
                    ) : (
                        <div className="flex justify-between items-center">
                            <div className="flex gap-4">
                                <Typography variant="h6">{car.brand}</Typography>
                                <Typography variant="paragraph">{car.model}</Typography>
                                <Typography variant="paragraph">{car.year}</Typography>
                                <Typography variant="paragraph">{car.totalKm} km</Typography>
                                <Typography variant="paragraph">{car.isElectric ? "Electric" : "Non-Electric"}</Typography>
                            </div>
                            <div className="flex gap-2">
                                <IconButton color="blue" onClick={() => handleEditClick(car)}>
                                    <FaPencilRuler className="h-5 w-5" />
                                </IconButton>
                                <IconButton color="red" onClick={() => handleDeleteCar(car._id)}>
                                    <MdDeleteForever className="h-5 w-5" />
                                </IconButton>
                            </div>
                        </div>
                    )}
                </li>
            ))}
        </ul>
    );
}
