import React from 'react';
import { Dialog, DialogBody, DialogFooter, DialogHeader } from '@material-tailwind/react';

const Modal = ({ isOpen, title, children, footer, onClose }) => {
  return (
    <Dialog open={isOpen} handler={onClose} size="lg">
      <DialogHeader>{title}</DialogHeader>
      <DialogBody divider>
        {children}
      </DialogBody>
      <DialogFooter>
        {footer}
      </DialogFooter>
    </Dialog>
  );
};

export default Modal;
