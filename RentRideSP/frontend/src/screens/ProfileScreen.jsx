import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { Typography, Card, Button, List, ListItem } from '@material-tailwind/react';
import UpdateDetailsForm from '../components/UpdateDetailsForm';
import UpdatePasswordForm from '../components/UpdatePasswordForm';
import { useGetAllRidesQuery, useUpdateUserProfilePicMutation, useGetUserByIdQuery } from '../slices/usersApiSlice';

export function ProfileScreen() {
  const userInfo = useSelector((state) => state.auth.userInfo);
  const [showUpdateDetails, setShowUpdateDetails] = useState(false);
  const [showUpdatePassword, setShowUpdatePassword] = useState(false);
  const [profilePic, setProfilePic] = useState('https://storage.googleapis.com/rentride-1df1d.appspot.com/1716825620081.jpg');
  const [uploadProfilePic] = useUpdateUserProfilePicMutation();

  const { data: user, isLoading: userLoading } = useGetUserByIdQuery(userInfo?._id);
  const { data: rides = [], isLoading: ridesLoading } = useGetAllRidesQuery();

  useEffect(() => {
    if (user && user.profilepic) {
      setProfilePic(user.profilepic);
    }
  }, [user]);

  if (userLoading) {
    return <Typography variant="h6" className="text-center">Loading user information...</Typography>;
  }

  if (!userInfo) {
    return <Typography variant="h6" className="text-center">No user information available.</Typography>;
  }

  const handleProfilePicChange = async (e) => {
    const file = e.target.files[0];
    const formData = new FormData();
    formData.append('profilePic', file);
    try {
      const response = await uploadProfilePic({ id: userInfo._id, formData }).unwrap();
      setProfilePic(response.profilepic);
    } catch (error) {
      console.error('Error uploading profile picture:', error);
    }
  };

  const userRides = rides.filter(ride => ride.driver._id === userInfo._id);
  const carsDriven = [...new Set(userRides.map(ride => JSON.stringify(ride.car)))].map(car => JSON.parse(car));

  return (
    <Card className="p-6 mx-auto max-w-screen-md mt-10">
      <Typography variant="h4" className="text-center mb-4">Profile</Typography>
      <div className="mb-4">
        <Typography variant="h6">Profile Picture:</Typography>
        <img
          src={profilePic}
          alt="Profile"
          className="w-32 h-32 rounded-full"
        />
        <input type="file" onChange={handleProfilePicChange} />
      </div>
      <div className="mb-4">
        <Typography variant="h6">Username: {user.username}</Typography>
      </div>
      <div className="mb-4">
        <Typography variant="h6">Email: {user.email}</Typography>
      </div>
      
      <div className="mb-4 flex justify-center space-x-4">
        <Button onClick={() => setShowUpdateDetails(true)}>Update Details</Button>
        <Button onClick={() => setShowUpdatePassword(true)}>Update Password</Button>
      </div>
      {showUpdateDetails && (
        <UpdateDetailsForm
          isOpen={showUpdateDetails}
          onClose={() => setShowUpdateDetails(false)}
        />
      )}
      {showUpdatePassword && (
        <UpdatePasswordForm
          isOpen={showUpdatePassword}
          onClose={() => setShowUpdatePassword(false)}
        />
      )}
      <div className="mt-8">
        <Typography variant="h5" className="mb-4">Rides</Typography>
        {ridesLoading ? (
          <Typography>Loading rides...</Typography>
        ) : (
          <List>
            {userRides.map(ride => (
              <ListItem key={ride._id}>
                {new Date(ride.createdAt).toLocaleDateString()} - {ride.car.make} {ride.car.model}
              </ListItem>
            ))}
          </List>
        )}
      </div>
      <div className="mt-8">
        <Typography variant="h5" className="mb-4">Cars Driven</Typography>
        {ridesLoading ? (
          <Typography>Loading cars...</Typography>
        ) : (
          <List>
            {carsDriven.map(car => (
              <ListItem key={car._id}>
                {car.make} {car.model}
              </ListItem>
            ))}
          </List>
        )}
      </div>
    </Card>
  );
}

export default ProfileScreen;
