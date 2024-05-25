import React from 'react';
import { GoogleMap, DirectionsRenderer } from '@react-google-maps/api';

const containerStyle = {
    width: '100%',
    height: '400px',
};

const center = {
    lat: 46.55913376337574,
    lng: 15.638080093732981,
};

const Map = ({ directionsResponse }) => {
    return (
        <GoogleMap mapContainerStyle={containerStyle} center={center} zoom={10}>
            {directionsResponse && (
                <DirectionsRenderer directions={directionsResponse} />
            )}
        </GoogleMap>
    );
};

export default Map;
