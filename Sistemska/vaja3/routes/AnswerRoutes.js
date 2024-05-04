var express = require('express');
var router = express.Router();
var AnswerController = require('../controllers/AnswerController.js');

/*
 * GET
 */
router.get('/', AnswerController.list);

/*
 * GET
 */
router.get('/:id', AnswerController.show);

/*
 * POST
 */
router.post('/', AnswerController.create);
router.post('/postAnswer', AnswerController.postAnswer);
router.post('/postAnswerMy', AnswerController.postAnswerMy);

router.post('/upvoteAnswer/:answerId/upvote', AnswerController.upvoteAnswer);
router.post('/upvoteAnswerMy/:answerId/upvote', AnswerController.upvoteAnswerMy);

router.post('/downvoteAnswer/:answerId/downvote', AnswerController.downvoteAnswer);
router.post('/downvoteAnswerMy/:answerId/downvote', AnswerController.downvoteAnswerMy);

router.post('/markAsCorrect/:answerId', AnswerController.markAsCorrect);

router.post('/delete/:id', AnswerController.remove);

/*
 * PUT
 */
router.put('/:id', AnswerController.update);

/*
 * DELETE
 */
router.delete('/:id', AnswerController.remove);

module.exports = router;
