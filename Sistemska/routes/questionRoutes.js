var express = require('express');
var router = express.Router();
var questionController = require('../controllers/questionController.js');

/*
 * GET
 */
router.get('/', questionController.list);
router.get('/addQuestions', questionController.addQuestions);
router.get('/displayQuestions', questionController.displayQuestions);
router.get('/displayPopularQuestions', questionController.displayPopularQuestions);


router.get('/displayQuestion/:id', questionController.displayQuestion);
/*
 * GET
 */
router.get('/:id', questionController.show);

/*
 * POST
 */
router.post('/', questionController.create);

/*
 * PUT
 */
router.put('/:id', questionController.update);

/*
 * DELETE
 */
router.delete('/:id', questionController.remove);

module.exports = router;
