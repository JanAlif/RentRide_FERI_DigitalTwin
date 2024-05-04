var express = require('express');
var router = express.Router();
var CommentController = require('../controllers/CommentController.js');

/*
 * GET
 */
router.get('/', CommentController.list);

/*
 * GET
 */
router.get('/:id', CommentController.show);

/*
 * POST
 */
router.post('/', CommentController.create);
router.post('/postComment', CommentController.postComment);
router.post('/postCommentMy', CommentController.postCommentMy);

router.post('/postAnswerComment', CommentController.postAnswerComment);
router.post('/postAnswerCommentMy', CommentController.postAnswerCommentMy);


/*
 * PUT
 */
router.put('/:id', CommentController.update);

/*
 * DELETE
 */
router.delete('/:id', CommentController.remove);

module.exports = router;
