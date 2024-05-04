var express = require('express');
var router = express.Router();
var QuestionController = require('../controllers/QuestionController.js');

/*
 * GET
 */
router.get('/', QuestionController.list);
router.get('/postQuestion', QuestionController.showPostQuestion);
router.get('/questionDetail/:id', function(req, res, next) {
    QuestionController.incrementViewsAndShowDetail(req, res, next);
});

router.get('/questionDetail/:id', QuestionController.showQuestionDetail);

router.get('/activityQuestionDetail/:id', QuestionController.showActivityQuestionDetail);

router.get('/showHotQuestions/', QuestionController.showHotQuestions);

/*
 * GET
 */
router.get('/:id', QuestionController.show);

/*
 * POST
 */
router.post('/', QuestionController.create);
router.post('/postQuestion', QuestionController.postQuestion)

router.post('/delete/:id', QuestionController.remove);

/*
 * PUT
 */
router.put('/:id', QuestionController.update);

/*
 * DELETE
 */
router.delete('/:id', QuestionController.remove);

module.exports = router;
