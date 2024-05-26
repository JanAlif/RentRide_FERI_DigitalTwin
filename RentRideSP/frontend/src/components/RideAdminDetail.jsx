import React, { useState, useEffect } from 'react';
import { Typography, Button, Input, IconButton, Select, Option } from '@material-tailwind/react';
import { useUpdateRideMutation } from '../slices/adminSlice';
import { FaPencilRuler } from "react-icons/fa";
import { MdDeleteForever } from "react-icons/md";

const validStatuses = ["pending", "started", "completed", "cancelled"];

export function RideAdminDetail({ rides, handleDeleteRide, users, cars }) {
    const [editingRideId, setEditingRideId] = useState(null);
    const [updatedRide, setUpdatedRide] = useState({ driverId: '', carId: '', startLocation: '', endLocation: '', status: '' });
    const [updateRide] = useUpdateRideMutation();
    const [localRides, setLocalRides] = useState([]);

    useEffect(() => {
        setLocalRides(rides);
    }, [rides]);

    const handleEditClick = (ride) => {
        setEditingRideId(ride._id);
        setUpdatedRide({
            driverId: ride.driver?._id || '',
            carId: ride.car?._id || '',
            startLocation: ride.startLocation?.coordinates.join(',') || '',
            endLocation: ride.endLocation?.coordinates.join(',') || '',
            status: ride.status,
        });
    };

    const handleUpdateRide = async (id) => {
        if (!updatedRide.driverId || !updatedRide.carId || !validStatuses.includes(updatedRide.status)) {
            console.error('Driver ID, Car ID, or Status is invalid');
            return;
        }

        try {
            const startLocation = {
                type: "Point",
                coordinates: updatedRide.startLocation.split(',').map(Number)
            };
            const endLocation = {
                type: "Point",
                coordinates: updatedRide.endLocation.split(',').map(Number)
            };
            const rideData = {
                ...updatedRide,
                startLocation: JSON.stringify(startLocation),
                endLocation: JSON.stringify(endLocation),
            };

            console.log('Ride data being sent:', rideData); // Debug log
            await updateRide({ id, updatedRide: rideData }).unwrap();
            setLocalRides(prevRides =>
                prevRides.map(ride =>
                    ride._id === id ? { ...ride, ...rideData, startLocation, endLocation } : ride
                )
            );
            setEditingRideId(null);
        } catch (error) {
            console.error('Error updating ride:', error);
        }
    };

    return (
        <ul>
            {localRides.map(ride => (
                <li key={ride._id} className="flex flex-col mb-2">
                    {editingRideId === ride._id ? (
                        <div className="flex gap-4 mb-2">
                            <Select
                                label="Driver"
                                value={updatedRide.driverId}
                                onChange={(e) => {
                                    console.log('Selected Driver ID:', e);
                                    setUpdatedRide({ ...updatedRide, driverId: e });
                                }}
                            >
                                {users.map(user => (
                                    <Option key={user._id} value={user._id}>{user.username}</Option>
                                ))}
                            </Select>
                            <Select
                                label="Car"
                                value={updatedRide.carId}
                                onChange={(e) => {
                                    console.log('Selected Car ID:', e);
                                    setUpdatedRide({ ...updatedRide, carId: e });
                                }}
                            >
                                {cars.map(car => (
                                    <Option key={car._id} value={car._id}>{car.model}</Option>
                                ))}
                            </Select>
                            <Input
                                label="Start Location"
                                value={updatedRide.startLocation}
                                onChange={(e) => setUpdatedRide({ ...updatedRide, startLocation: e.target.value })}
                            />
                            <Input
                                label="End Location"
                                value={updatedRide.endLocation}
                                onChange={(e) => setUpdatedRide({ ...updatedRide, endLocation: e.target.value })}
                            />
                            <Select
                                label="Status"
                                value={updatedRide.status}
                                onChange={(e) => setUpdatedRide({ ...updatedRide, status: e })}
                            >
                                {validStatuses.map(status => (
                                    <Option key={status} value={status}>{status}</Option>
                                ))}
                            </Select>
                            <div className="flex justify-between">
                                <Button onClick={() => handleUpdateRide(ride._id)}>Save</Button>
                                <Button color="red" onClick={() => setEditingRideId(null)}>Cancel</Button>
                            </div>
                        </div>
                    ) : (
                        <div className="flex justify-between items-center">
                            <div className="flex gap-4">
                                <Typography variant="h6">{ride.driver?.username || 'Unknown Driver'}</Typography>
                                <Typography variant="paragraph">{ride.car?.model || 'Unknown Car'}</Typography>
                                <Typography variant="paragraph">{new Date(ride.startTime).toLocaleString()}</Typography>
                                <Typography variant="paragraph">{ride.status}</Typography>
                            </div>
                            <div className="flex gap-2">
                                <IconButton color="blue" onClick={() => handleEditClick(ride)}>
                                    <FaPencilRuler className="h-5 w-5" />
                                </IconButton>
                                <IconButton color="red" onClick={() => handleDeleteRide(ride._id)}>
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
