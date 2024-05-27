import { createRequire } from 'module';
const require = createRequire(import.meta.url);

const admin = require('firebase-admin');
const serviceAccount = require('./firebaseServiceAccount.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: 'rentride-1df1d.appspot.com' // Ensure this matches your actual bucket name
});

const bucket = admin.storage().bucket();

export { bucket };
