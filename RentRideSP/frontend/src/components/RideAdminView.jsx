import React, { useState, useEffect } from 'react';
import { Card, Typography, Button, Input, Select, Option, IconButton } from '@material-tailwind/react';
import { MdDeleteForever } from 'react-icons/md';
import { useGetAllRidesQuery, useAddRideMutation, useDeleteRideMutation, useGetAllUsersQuery, useGetAllCarsQuery } from '../slices/adminSlice';
import { RideAdminDetail } from './RideAdminDetail';
import ConfirmDialog from './ConfirmDialog';  // Import the new ConfirmDialog component

export function RideAdminView() {
    const { data: rides = [], refetch } = useGetAllRidesQuery();
    const { data: users = [] } = useGetAllUsersQuery();
    const { data: cars = [] } = useGetAllCarsQuery();
    const [addRide] = useAddRideMutation();
    const [deleteRide] = useDeleteRideMutation();
    const [newRide, setNewRide] = useState({ driverId: '', carId: '', startLocation: '', endLocation: '' });
    const [selectedDriver, setSelectedDriver] = useState(null);
    const [selectedCar, setSelectedCar] = useState(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [rideToDelete, setRideToDelete] = useState(null);

    useEffect(() => {
        if (newRide.driverId) {
            const driver = users.find(user => user._id === newRide.driverId);
            setSelectedDriver(driver);
        } else {
            setSelectedDriver(null);
        }
    }, [newRide.driverId, users]);

    useEffect(() => {
        if (newRide.carId) {
            const car = cars.find(car => car._id === newRide.carId);
            setSelectedCar(car);
        } else {
            setSelectedCar(null);
        }
    }, [newRide.carId, cars]);

    const handleAddRide = async () => {
        if (!newRide.driverId || !newRide.carId) {
            console.error('Driver ID or Car ID is missing');
            return;
        }

        try {
            const rideData = {
                ...newRide,
                startLocation: JSON.stringify(newRide.startLocation.split(',').map(Number)),
                endLocation: JSON.stringify(newRide.endLocation.split(',').map(Number)),
            };
            await addRide(rideData).unwrap();
            setNewRide({ driverId: '', carId: '', startLocation: '', endLocation: '' });
            refetch(); // Refetch rides after adding a new ride
        } catch (error) {
            console.error('Error adding ride:', error);
        }
    };

    const handleDeleteRide = async (id) => {
        try {
            await deleteRide(id).unwrap();
            refetch(); // Refetch rides after deleting a ride
        } catch (error) {
            console.error('Error deleting ride:', error);
        }
    };

    const openDeleteDialog = (rideId) => {
        setRideToDelete(rideId);
        setDeleteDialogOpen(true);
    };

    const closeDeleteDialog = () => {
        setRideToDelete(null);
        setDeleteDialogOpen(false);
    };

    const confirmDeleteRide = () => {
        if (rideToDelete) {
            handleDeleteRide(rideToDelete);
            closeDeleteDialog();
        }
    };

    return (
        <Card className="p-6">
            <Typography variant="h4" color="blue-gray" className="text-center mb-4">
                Ride Management
            </Typography>
            <div className="mb-4 space-y-2">
                <div className="flex items-center space-x-4">
                    <Select
                        label="Driver"
                        value={newRide.driverId}
                        onChange={(val) => setNewRide({ ...newRide, driverId: val })}
                        className="flex-1"
                    >
                        {users.map(user => (
                            <Option key={user._id} value={user._id}>{user.username}</Option>
                        ))}
                    </Select>
                    {selectedDriver && (
                        <Typography variant="body2" className="ml-4">
                            {selectedDriver.username}
                        </Typography>
                    )}
                </div>
                <div className="flex items-center space-x-4">
                    <Select
                        label="Car"
                        value={newRide.carId}
                        onChange={(val) => setNewRide({ ...newRide, carId: val })}
                        className="flex-1"
                    >
                        {cars.map(car => (
                            <Option key={car._id} value={car._id}>{car.model}</Option>
                        ))}
                    </Select>
                    {selectedCar && (
                        <Typography variant="body2" className="ml-4">
                            {selectedCar.model}
                        </Typography>
                    )}
                </div>
                <Input
                    label="Start Location"
                    placeholder="latitude,longitude"
                    value={newRide.startLocation}
                    onChange={(e) => setNewRide({ ...newRide, startLocation: e.target.value })}
                />
                <Input
                    label="End Location"
                    placeholder="latitude,longitude"
                    value={newRide.endLocation}
                    onChange={(e) => setNewRide({ ...newRide, endLocation: e.target.value })}
                />
                <Button onClick={handleAddRide} className="mt-2">
                    Add Ride
                </Button>
            </div>
            <RideAdminDetail rides={rides} handleDeleteRide={openDeleteDialog} users={users} cars={cars} />

            <ConfirmDialog
                open={deleteDialogOpen}
                onClose={closeDeleteDialog}
                onConfirm={confirmDeleteRide}
                title="Confirm Deletion"
                message="Are you sure you want to delete this ride?"
            />
        </Card>
    );
}
