import React, { useState } from 'react';
import { Card, Typography, Button, Input, Switch } from '@material-tailwind/react';
import { useGetAllCarsQuery, useAddCarMutation, useDeleteCarMutation } from '../slices/adminSlice';
import { CarAdminDetail } from './CarAdminDetail';

export function CarAdminView() {
    const { data: cars = [], refetch } = useGetAllCarsQuery();
    const [addCar] = useAddCarMutation();
    const [deleteCar] = useDeleteCarMutation();
    const [newCar, setNewCar] = useState({ brand: '', model: '', year: '', totalKm: '', isElectric: false, location: '' });

    const handleAddCar = async () => {
        try {
            const carData = {
                ...newCar,
                year: Number(newCar.year),
                totalKm: Number(newCar.totalKm),
                location: newCar.location ? JSON.stringify(newCar.location.split(',').map(Number)) : undefined
            };
            await addCar(carData).unwrap();
            setNewCar({ brand: '', model: '', year: '', totalKm: '', isElectric: false, location: '' });
            refetch(); // Refetch cars after adding a new car
        } catch (error) {
            console.error('Error adding car:', error);
        }
    };

    const handleDeleteCar = async (id) => {
        try {
            await deleteCar(id).unwrap();
            refetch(); // Refetch cars after deleting a car
        } catch (error) {
            console.error('Error deleting car:', error);
        }
    };

    return (
        <Card className="p-6">
            <Typography variant="h4" color="blue-gray" className="text-center mb-4">
                Car Management
            </Typography>
            <div className="mb-4 space-y-2">
                <Input
                    label="Brand"
                    value={newCar.brand}
                    onChange={(e) => setNewCar({ ...newCar, brand: e.target.value })}
                />
                <Input
                    label="Model"
                    value={newCar.model}
                    onChange={(e) => setNewCar({ ...newCar, model: e.target.value })}
                />
                <Input
                    label="Year"
                    type="number"
                    value={newCar.year}
                    onChange={(e) => setNewCar({ ...newCar, year: e.target.value })}
                />
                <Input
                    label="Total Km"
                    type="number"
                    value={newCar.totalKm}
                    onChange={(e) => setNewCar({ ...newCar, totalKm: e.target.value })}
                />
                <div className="flex items-center">
                    <Typography className="mr-2">Electric</Typography>
                    <Switch
                        checked={newCar.isElectric}
                        onChange={(e) => setNewCar({ ...newCar, isElectric: e.target.checked })}
                    />
                </div>
                <Input
                    label="Location"
                    placeholder="latitude,longitude"
                    value={newCar.location}
                    onChange={(e) => setNewCar({ ...newCar, location: e.target.value })}
                />
                <Button onClick={handleAddCar} className="mt-2">
                    Add Car
                </Button>
            </div>
            <CarAdminDetail cars={cars} handleDeleteCar={handleDeleteCar} />
        </Card>
    );
}
