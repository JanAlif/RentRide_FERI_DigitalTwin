import React, { useState, useEffect } from 'react';
import { Typography, Button, Input, IconButton } from '@material-tailwind/react';
import { useUpdateChargePointMutation } from '../slices/adminSlice';
import { FaPencilRuler } from "react-icons/fa";
import { MdDeleteForever } from "react-icons/md";

export function ChargepointAdminDetail({ chargePoints, handleDeleteChargePoint }) {
    const [editingChargePointId, setEditingChargePointId] = useState(null);
    const [updatedChargePoint, setUpdatedChargePoint] = useState({ locationName: '', address: '', coordinates: '', connectors: 0, connectorsAvailable: 0 });
    const [updateChargePoint] = useUpdateChargePointMutation();
    const [localChargePoints, setLocalChargePoints] = useState([]);

    useEffect(() => {
        setLocalChargePoints(chargePoints);
    }, [chargePoints]);

    const handleEditClick = (chargePoint) => {
        setEditingChargePointId(chargePoint._id);
        setUpdatedChargePoint({
            locationName: chargePoint.locationName,
            address: chargePoint.address,
            coordinates: chargePoint.location.coordinates.join(','),
            connectors: chargePoint.connectors,
            connectorsAvailable: chargePoint.connectorsAvailable,
        });
    };

    const handleUpdateChargePoint = async (id) => {
        try {
            const chargePointData = {
                ...updatedChargePoint,
                coordinates: JSON.stringify(updatedChargePoint.coordinates.split(',').map(Number)),
            };
            await updateChargePoint({ id, updatedChargePoint: chargePointData }).unwrap();
            setLocalChargePoints(prevChargePoints =>
                prevChargePoints.map(chargePoint =>
                    chargePoint._id === id ? { ...chargePoint, ...chargePointData } : chargePoint
                )
            );
            setEditingChargePointId(null);
        } catch (error) {
            console.error('Error updating charge point:', error);
        }
    };

    return (
        <ul>
            {localChargePoints.map(chargePoint => (
                <li key={chargePoint._id} className="flex flex-col mb-2">
                    {editingChargePointId === chargePoint._id ? (
                        <div className="flex gap-4 mb-2">
                            <Input
                                label="Location Name"
                                value={updatedChargePoint.locationName}
                                onChange={(e) => setUpdatedChargePoint({ ...updatedChargePoint, locationName: e.target.value })}
                            />
                            <Input
                                label="Address"
                                value={updatedChargePoint.address}
                                onChange={(e) => setUpdatedChargePoint({ ...updatedChargePoint, address: e.target.value })}
                            />
                            <Input
                                label="Coordinates"
                                value={updatedChargePoint.coordinates}
                                onChange={(e) => setUpdatedChargePoint({ ...updatedChargePoint, coordinates: e.target.value })}
                            />
                            <Input
                                label="Connectors"
                                type="number"
                                value={updatedChargePoint.connectors}
                                onChange={(e) => setUpdatedChargePoint({ ...updatedChargePoint, connectors: Number(e.target.value) })}
                            />
                            <Input
                                label="Connectors Available"
                                type="number"
                                value={updatedChargePoint.connectorsAvailable}
                                onChange={(e) => setUpdatedChargePoint({ ...updatedChargePoint, connectorsAvailable: Number(e.target.value) })}
                            />
                            <div className="flex justify-between">
                                <Button onClick={() => handleUpdateChargePoint(chargePoint._id)}>Save</Button>
                                <Button color="red" onClick={() => setEditingChargePointId(null)}>Cancel</Button>
                            </div>
                        </div>
                    ) : (
                        <div className="flex justify-between items-center">
                            <div className="flex gap-4">
                                <Typography variant="h6">{chargePoint.locationName}</Typography>
                                <Typography variant="paragraph">{chargePoint.address}</Typography>
                                <Typography variant="paragraph">{chargePoint.connectors} connectors</Typography>
                                <Typography variant="paragraph">{chargePoint.connectorsAvailable} available</Typography>
                            </div>
                            <div className="flex gap-2">
                                <IconButton color="blue" onClick={() => handleEditClick(chargePoint)}>
                                    <FaPencilRuler className="h-5 w-5" />
                                </IconButton>
                                <IconButton color="red" onClick={() => handleDeleteChargePoint(chargePoint._id)}>
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
