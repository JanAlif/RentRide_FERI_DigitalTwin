import express from 'express';
import Traffic from '../models/Traffic.js';

const router = express.Router();

// @desc    Get all traffic lines
// @route   GET /api/traffics
// @access  Public or Protected based on your authentication
router.get('/', async (req, res) => {
    try {
        const traffics = await Traffic.find({});
        res.json(traffics);
    } catch (error) {
        console.error('Error fetching traffics:', error);
        res.status(500).json({ message: 'Server Error' });
    }
});

export default router;