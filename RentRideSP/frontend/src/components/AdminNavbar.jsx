import React from 'react';
import { Navbar, Typography } from '@material-tailwind/react';

export function AdminNavbar({ setActiveComponent }) {
    return (
        <Navbar className="sticky top-0 z-10 h-16 bg-black text-white flex items-center px-4 -mt-10">
            <div className="flex justify-between items-left space-x-4">
                <Typography
                    variant="paragraph"
                    className="text-white cursor-pointer"
                    onClick={() => setActiveComponent('users')}
                >
                    Users
                </Typography>
                <Typography
                    variant="paragraph"
                    className="text-white cursor-pointer"
                    onClick={() => setActiveComponent('cars')}
                >
                    Cars
                </Typography>
                <Typography
                    variant="paragraph"
                    className="text-white cursor-pointer"
                    onClick={() => setActiveComponent('chargepoints')}
                >
                    Chargepoints
                </Typography>
                <Typography
                    variant="paragraph"
                    className="text-white cursor-pointer"
                    onClick={() => setActiveComponent('rides')}
                >
                    Rides
                </Typography>
            </div>
        </Navbar>
    );
}
