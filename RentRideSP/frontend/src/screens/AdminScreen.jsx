import React, { useState } from 'react';
import { Card, Typography } from '@material-tailwind/react';
import { AdminNavbar } from '../components/AdminNavbar'; // Adjust the path as necessary

import { UserAdminView } from '../components/UserAdminView'; // Adjust the path as necessary

export function AdminScreen() {
    const [activeComponent, setActiveComponent] = useState('home');

    return (
        <div className="relative">
            <AdminNavbar setActiveComponent={setActiveComponent} />
            <div className="flex">

                <div className="flex-grow p-4">
                    {activeComponent === 'home' && (
                        <Card color="transparent" shadow={false} className="mx-auto max-w-screen-2xl px-4 py-12">
                            <Typography variant="h4" color="blue-gray" className="text-center">
                                Hello Admin
                            </Typography>
                            <Typography color="gray" className="mt-4 text-center">
                                Welcome to the admin panel. Use the options above to manage your data.
                            </Typography>
                        </Card>
                    )}
                    {activeComponent === 'users' && <UserAdminView />}
                </div>
            </div>
        </div>
    );
}
