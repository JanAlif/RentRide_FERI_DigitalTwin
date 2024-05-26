import React, { useState } from 'react';
import { Card, Typography, Button, Input } from '@material-tailwind/react';
import { useGetAllChargePointsQuery, useAddChargePointMutation, useDeleteChargePointMutation } from '../slices/adminSlice';
import { ChargepointAdminDetail } from './ChargepointsAdminDetail';
import ConfirmDialog from './ConfirmDialog';  // Import the new ConfirmDialog component

export function ChargepointAdminView() {
    const { data: chargePoints = [], refetch } = useGetAllChargePointsQuery();
    const [addChargePoint] = useAddChargePointMutation();
    const [deleteChargePoint] = useDeleteChargePointMutation();
    const [newChargePoint, setNewChargePoint] = useState({ locationName: '', address: '', coordinates: '', connectors: 0, connectorsAvailable: 0 });
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [chargePointToDelete, setChargePointToDelete] = useState(null);

    const handleAddChargePoint = async () => {
        try {
            const chargePointData = {
                ...newChargePoint,
                coordinates: JSON.stringify(newChargePoint.coordinates.split(',').map(Number)),
            };
            await addChargePoint(chargePointData).unwrap();
            setNewChargePoint({ locationName: '', address: '', coordinates: '', connectors: 0, connectorsAvailable: 0 });
            refetch(); // Refetch charge points after adding a new charge point
        } catch (error) {
            console.error('Error adding charge point:', error);
        }
    };

    const handleDeleteChargePoint = async (id) => {
        try {
            await deleteChargePoint(id).unwrap();
            refetch(); // Refetch charge points after deleting a charge point
        } catch (error) {
            console.error('Error deleting charge point:', error);
        }
    };

    const openDeleteDialog = (chargePointId) => {
        setChargePointToDelete(chargePointId);
        setDeleteDialogOpen(true);
    };

    const closeDeleteDialog = () => {
        setChargePointToDelete(null);
        setDeleteDialogOpen(false);
    };

    const confirmDeleteChargePoint = () => {
        if (chargePointToDelete) {
            handleDeleteChargePoint(chargePointToDelete);
            closeDeleteDialog();
        }
    };

    return (
        <Card className="p-6">
            <Typography variant="h4" color="blue-gray" className="text-center mb-4">
                Charge Point Management
            </Typography>
            <div className="mb-4 space-y-2">
                <Input
                    label="Location Name"
                    value={newChargePoint.locationName}
                    onChange={(e) => setNewChargePoint({ ...newChargePoint, locationName: e.target.value })}
                />
                <Input
                    label="Address"
                    value={newChargePoint.address}
                    onChange={(e) => setNewChargePoint({ ...newChargePoint, address: e.target.value })}
                />
                <Input
                    label="Coordinates"
                    placeholder="latitude,longitude"
                    value={newChargePoint.coordinates}
                    onChange={(e) => setNewChargePoint({ ...newChargePoint, coordinates: e.target.value })}
                />
                <Input
                    label="Connectors"
                    type="number"
                    value={newChargePoint.connectors}
                    onChange={(e) => setNewChargePoint({ ...newChargePoint, connectors: Number(e.target.value) })}
                />
                <Input
                    label="Connectors Available"
                    type="number"
                    value={newChargePoint.connectorsAvailable}
                    onChange={(e) => setNewChargePoint({ ...newChargePoint, connectorsAvailable: Number(e.target.value) })}
                />
                <Button onClick={handleAddChargePoint} className="mt-2">
                    Add Charge Point
                </Button>
            </div>
            <ChargepointAdminDetail chargePoints={chargePoints} handleDeleteChargePoint={openDeleteDialog} />

            <ConfirmDialog
                open={deleteDialogOpen}
                onClose={closeDeleteDialog}
                onConfirm={confirmDeleteChargePoint}
                title="Confirm Deletion"
                message="Are you sure you want to delete this charge point?"
            />
        </Card>
    );
}
