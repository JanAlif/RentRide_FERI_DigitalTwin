import React, { useState, useEffect } from 'react';
import { Typography, Button, Input, IconButton } from '@material-tailwind/react';
import { useUpdateUserMutation } from '../slices/adminSlice';
import { FaPencilRuler  } from "react-icons/fa";
import { IoPersonRemove } from "react-icons/io5";

const defaultProfilePic = 'https://storage.googleapis.com/rentride-1df1d.appspot.com/1716825620081.jpg';

export function UserAdminDetail({ users, handleDeleteUser }) {
    const [editingUserId, setEditingUserId] = useState(null);
    const [updatedUser, setUpdatedUser] = useState({ username: '', email: '' });
    const [updateUser] = useUpdateUserMutation();
    const [localUsers, setLocalUsers] = useState([]);

    useEffect(() => {
        setLocalUsers(users);
    }, [users]);

    const handleEditClick = (user) => {
        setEditingUserId(user._id);
        setUpdatedUser({
            username: user.username,
            email: user.email,
            name: user.name || '',
        });
    };

    const handleUpdateUser = async (id) => {
        try {
            const userData = {
                username: updatedUser.username,
                email: updatedUser.email,
                name: updatedUser.name,
            };
            await updateUser({ id, updatedUser: userData }).unwrap();
            setLocalUsers(prevUsers =>
                prevUsers.map(user =>
                    user._id === id ? { ...user, ...userData } : user
                )
            );
            setEditingUserId(null);
        } catch (error) {
            console.error('Error updating user:', error);
        }
    };

    return (
        <ul>
            {localUsers.map(user => (
                <li key={user._id} className="flex flex-col mb-2">
                    {editingUserId === user._id ? (
                        <div className="flex gap-4 mb-2">
                            <Input
                                label="Username"
                                value={updatedUser.username}
                                onChange={(e) => setUpdatedUser({ ...updatedUser, username: e.target.value })}
                                className="mb-2"
                            />
                            <Input
                                label="Email"
                                value={updatedUser.email}
                                onChange={(e) => setUpdatedUser({ ...updatedUser, email: e.target.value })}
                                className="mb-2"
                            />
                            <Input
                                label="Name"
                                value={updatedUser.name}
                                onChange={(e) => setUpdatedUser({ ...updatedUser, name: e.target.value })}
                                className="mb-2"
                            />
                            <div className="flex justify-between">
                                <Button onClick={() => handleUpdateUser(user._id)}>Save</Button>
                                <Button color="red" onClick={() => setEditingUserId(null)}>Cancel</Button>
                            </div>
                        </div>
                    ) : (
                        <div className="flex justify-between items-center">
                            <div className="flex items-center gap-4">
                                <img
                                    src={user.profilepic || defaultProfilePic}
                                    alt="Profile"
                                    className="w-10 h-10 rounded-full"
                                />
                                <div className="flex gap-4 mb-2">
                                    <Typography variant="h6">{user.username}</Typography>
                                    <Typography variant="paragraph">{user.email}</Typography>
                                </div>
                            </div>
                            <div className="flex gap-2">
                                <IconButton color="blue" onClick={() => handleEditClick(user)}>
                                    <FaPencilRuler className="h-5 w-5" />
                                </IconButton>
                                <IconButton color="red" onClick={() => handleDeleteUser(user._id)}>
                                    <IoPersonRemove className="h-5 w-5" />
                                </IconButton>
                            </div>
                        </div>
                    )}
                </li>
            ))}
        </ul>
    );
}
