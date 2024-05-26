import React from 'react';
import { Dialog, DialogHeader, DialogBody, DialogFooter, Button } from '@material-tailwind/react';

const ConfirmDialog = ({ open, onClose, onConfirm, title, message }) => {
    return (
        <Dialog open={open} handler={onClose}>
            <DialogHeader>{title}</DialogHeader>
            <DialogBody>{message}</DialogBody>
            <DialogFooter>
                <Button variant="text" color="blue-gray" onClick={onClose}>
                    Cancel
                </Button>
                <Button variant="gradient" color="red" onClick={onConfirm}>
                    Confirm
                </Button>
            </DialogFooter>
        </Dialog>
    );
};

export default ConfirmDialog;
