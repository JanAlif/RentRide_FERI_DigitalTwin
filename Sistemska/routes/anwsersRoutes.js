var express = require('express');
var router = express.Router();
var anwsersController = require('../controllers/anwsersController.js');

/*
 * GET
 */
router.get('/', anwsersController.list);

/*
 * GET
 */
router.get('/:id', anwsersController.show);

/*
 * POST
 */
router.post('/', anwsersController.create);
router.post('/addAnwser', anwsersController.addAnwser);

/*
 * PUT
 */
router.put('/upvote', anwsersController.upvote);
router.put('/downvote', anwsersController.downvote);
router.put('/approve', anwsersController.approve);
router.put('/dissapprove', anwsersController.dissapprove);
router.put('/:id', anwsersController.update);

/*
 * DELETE
 */
router.delete('/deleteA/:id', anwsersController.deleteA);
router.delete('/:id', anwsersController.remove);

module.exports = router;
