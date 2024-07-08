import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useUpdateUserMutation } from '../slices/usersApiSlice';
import { setUser } from '../slices/authSlice';
import { Button } from '@material-tailwind/react';
import Modal from './Modal';

const LoadCreditsModal = ({ isOpen, onClose }) => {
  const dispatch = useDispatch();
  const userInfo = useSelector((state) => state.auth.userInfo);
  const [updateUser] = useUpdateUserMutation();

  const [selectedCredits, setSelectedCredits] = useState(null);
  const [message, setMessage] = useState(null);

  const creditOptions = [10, 20, 50, 100, 200];

  const handleSelectCredits = (credits) => {
    setSelectedCredits(credits);
  };

  const handleSubmit = async () => {
    if (!selectedCredits) {
      setMessage('Please select an amount of credits.');
      return;
    }

    try {
      const updatedUser = await updateUser({
        id: userInfo._id,
        credit: userInfo.credit + selectedCredits
      }).unwrap();
      dispatch(setUser(updatedUser));
      setMessage('Credits loaded successfully');
      onClose();
    } catch (error) {
      console.error('Failed to load credits:', error);
      setMessage('Failed to load credits');
    }
  };

  return (
    <Modal isOpen={isOpen} title="Load Credits" onClose={onClose} footer={message}>
      <div className="flex flex-col items-center">
        <div className="mb-4">
          {creditOptions.map((credits) => (
            <Button
              key={credits}
              onClick={() => handleSelectCredits(credits)}
              className={`m-2 ${selectedCredits === credits ? 'bg-blue-500 text-white' : ''}`}
            >
              {credits} Credits
            </Button>
          ))}
        </div>
        <div className="flex justify-end space-x-2">
          <Button variant="text" color="red" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={handleSubmit}>Confirm</Button>
        </div>
      </div>
    </Modal>
  );
};

export default LoadCreditsModal;
