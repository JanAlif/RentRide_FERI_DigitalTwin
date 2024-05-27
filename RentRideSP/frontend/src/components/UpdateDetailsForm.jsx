import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useUpdateUserMutation } from '../slices/usersApiSlice';
import { setUser } from '../slices/authSlice';
import { Input, Button } from '@material-tailwind/react';
import Modal from './Modal';

const UpdateDetailsForm = ({ isOpen, onClose }) => {
  const dispatch = useDispatch();
  const userInfo = useSelector((state) => state.auth.userInfo);
  const [updateUser] = useUpdateUserMutation();

  const [formData, setFormData] = useState({
    username: userInfo.username,
    email: userInfo.email,
    name: userInfo.name || '',
  });
  
  const [message, setMessage] = useState(null);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const updatedUser = await updateUser({ id: userInfo._id, ...formData }).unwrap();
      dispatch(setUser(updatedUser));
      setMessage('User details updated successfully');
    } catch (error) {
      console.error('Failed to update user:', error);
      setMessage('Failed to update user details');
    }
  };

  return (
    <Modal isOpen={isOpen} title="Update Details" onClose={onClose} footer={message}>
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <Input
            label="Username"
            name="username"
            value={formData.username}
            onChange={handleChange}
          />
        </div>
        <div className="mb-4">
          <Input
            label="Email"
            name="email"
            value={formData.email}
            onChange={handleChange}
          />
        </div>
        <div className="mb-4">
          <Input
            label="Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
          />
        </div>
        <div className="flex justify-end space-x-2">
          <Button variant="text" color="red" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit">Update Details</Button>
        </div>
      </form>
    </Modal>
  );
};

export default UpdateDetailsForm;
