var express = require('express');
var router = express.Router();
var photosController = require('../controllers/photosController.js');
var multer = require('multer');
var upload = multer({ dest: 'public/images' })

/*
 * GET
 */
router.get('/', photosController.list);

/*
 * GET
 */
router.get('/:id', photosController.show);

/*
 * POST
 */
router.post('/', photosController.create);

/*
 * PUT
 */
router.put('/:id', photosController.update);

/*
 * DELETE
 */
router.delete('/:id', photosController.remove);

module.exports = router;
