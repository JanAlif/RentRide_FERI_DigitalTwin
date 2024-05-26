import React, { useState } from 'react';
import { Card, Typography, Button, Input } from '@material-tailwind/react';
import { useGetAllUsersQuery, useAddUserMutation, useDeleteUserMutation } from '../slices/adminSlice';
import { UserAdminDetail } from './UserAdminDetail';
import ConfirmDialog from './ConfirmDialog';  // Import the new ConfirmDialog component

export function UserAdminView() {
    const { data: users = [], refetch } = useGetAllUsersQuery();
    const [addUser] = useAddUserMutation();
    const [deleteUser] = useDeleteUserMutation();
    const [newUser, setNewUser] = useState({ username: '', email: '', password: '' });
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    const handleAddUser = async () => {
        try {
            await addUser(newUser).unwrap();
            setNewUser({ username: '', email: '', password: '' });
            refetch(); // Refetch users after adding a new user
        } catch (error) {
            console.error('Error adding user:', error);
        }
    };

    const handleDeleteUser = async (id) => {
        try {
            await deleteUser(id).unwrap();
            refetch(); // Refetch users after deleting a user
        } catch (error) {
            console.error('Error deleting user:', error);
        }
    };

    const openDeleteDialog = (userId) => {
        setUserToDelete(userId);
        setDeleteDialogOpen(true);
    };

    const closeDeleteDialog = () => {
        setUserToDelete(null);
        setDeleteDialogOpen(false);
    };

    const confirmDeleteUser = () => {
        if (userToDelete) {
            handleDeleteUser(userToDelete);
            closeDeleteDialog();
        }
    };

    return (
        <Card className="p-6">
            <Typography variant="h4" color="blue-gray" className="text-center mb-4">
                User Management
            </Typography>
            <div className="mb-4 space-y-2">
                <Input
                    label="Username"
                    value={newUser.username}
                    onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                    className="mb-2"
                />
                <Input
                    label="Email"
                    value={newUser.email}
                    onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                    className="mb-2"
                />
                <Input
                    label="Password"
                    type="password"
                    value={newUser.password}
                    onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                    className="mb-2"
                />
                <Button onClick={handleAddUser} className="mt-2">
                    Add User
                </Button>
            </div>
            <UserAdminDetail users={users} handleDeleteUser={openDeleteDialog} />

            <ConfirmDialog
                open={deleteDialogOpen}
                onClose={closeDeleteDialog}
                onConfirm={confirmDeleteUser}
                title="Confirm Deletion"
                message="Are you sure you want to delete this user?"
            />
        </Card>
    );
}
