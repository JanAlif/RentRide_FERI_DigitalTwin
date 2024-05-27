import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { useUpdateUserPasswordMutation } from '../slices/usersApiSlice';
import { Input, Button, Typography } from '@material-tailwind/react';
import Modal from './Modal';

const UpdatePasswordForm = ({ isOpen, onClose }) => {
  const userInfo = useSelector((state) => state.auth.userInfo);
  const [updateUserPassword] = useUpdateUserPasswordMutation();

  const [formData, setFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [message, setMessage] = useState(null);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.newPassword !== formData.confirmPassword) {
      setMessage('New passwords do not match');
      return;
    }
    try {
      await updateUserPassword({
        id: userInfo._id,
        oldPassword: formData.oldPassword,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      }).unwrap();
      setMessage('Password updated successfully');
    } catch (error) {
      console.error('Failed to update password:', error);
      setMessage('Failed to update password');
    }
  };

  return (
    <Modal isOpen={isOpen} title="Update Password" onClose={onClose} footer={message}>
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <Input
            label="Old Password"
            type="password"
            name="oldPassword"
            value={formData.oldPassword}
            onChange={handleChange}
          />
        </div>
        <div className="mb-4">
          <Input
            label="New Password"
            type="password"
            name="newPassword"
            value={formData.newPassword}
            onChange={handleChange}
          />
        </div>
        <div className="mb-4">
          <Input
            label="Confirm New Password"
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
          />
        </div>
        <div className="flex justify-end space-x-2">
          <Button variant="text" color="red" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit">Update Password</Button>
        </div>
        
      </form>
    </Modal>
  );
};

export default UpdatePasswordForm;
