import React, { useState } from 'react';
import { Card, Typography, Button, Input, Switch } from '@material-tailwind/react';
import { useGetAllCarsQuery, useAddCarMutation, useDeleteCarMutation } from '../slices/adminSlice';
import { CarAdminDetail } from './CarAdminDetail';
import ConfirmDialog from './ConfirmDialog';  // Import the new ConfirmDialog component

export function CarAdminView() {
    const { data: cars = [], refetch } = useGetAllCarsQuery();
    const [addCar] = useAddCarMutation();
    const [deleteCar] = useDeleteCarMutation();
    const [newCar, setNewCar] = useState({ brand: '', model: '', year: '', totalKm: '', isElectric: false, location: '' });
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [carToDelete, setCarToDelete] = useState(null);

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

    const openDeleteDialog = (carId) => {
        setCarToDelete(carId);
        setDeleteDialogOpen(true);
    };

    const closeDeleteDialog = () => {
        setCarToDelete(null);
        setDeleteDialogOpen(false);
    };

    const confirmDeleteCar = () => {
        if (carToDelete) {
            handleDeleteCar(carToDelete);
            closeDeleteDialog();
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
            <CarAdminDetail cars={cars} handleDeleteCar={openDeleteDialog} />

            <ConfirmDialog
                open={deleteDialogOpen}
                onClose={closeDeleteDialog}
                onConfirm={confirmDeleteCar}
                title="Confirm Deletion"
                message="Are you sure you want to delete this car?"
            />
        </Card>
    );
}
