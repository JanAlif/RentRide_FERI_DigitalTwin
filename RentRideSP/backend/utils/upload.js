import multer from 'multer';
import path from 'path';
import { bucket } from '../config/firebaseConfig.js';

const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

const uploadImage = (file) => {
  return new Promise((resolve, reject) => {
    if (!file) {
      return reject(new Error('No file provided'));
    }

    const blob = bucket.file(Date.now() + path.extname(file.originalname));
    const blobStream = blob.createWriteStream({
      metadata: {
        contentType: file.mimetype
      }
    });

    blobStream.on('error', (err) => {
      console.error('Blob stream error:', err);
      reject(err);
    });

    blobStream.on('finish', async () => {
      // Make the file publicly accessible
      await blob.makePublic();
      const publicUrl = `https://storage.googleapis.com/${bucket.name}/${blob.name}`;
      resolve(publicUrl);
    });

    blobStream.end(file.buffer);
  });
};

export { upload, uploadImage };
