var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');
//var addProfilePictureToResponse = require('../controllers/userController.js/addProfilePictureToResponse'); // Adjust the path as necessary
/*
 * GET
 */
router.get('/', userController.list);

router.get('/login', userController.showLogin);
router.get('/register', userController.showRegister);
router.get('/editProfile', userController.showEditProfile);

router.get('/myActivity', userController.showMyActivity);

router.get('/profile', userController.profile);
router.get('/logout', userController.logout);

/*
 * GET
 */
router.get('/:id', userController.show);

/*
 * POST
 */
router.post('/', userController.create);
router.post('/login', userController.login);

//router.get('/editProfile', userController.update);
router.post('/update', userController.update);
router.post('/updatePassword', userController.updatePassword);

router.post('/updatePhoto', userController.updatePhoto)

/*
 * PUT
 */
router.put('/:id', userController.update);

/*
 * DELETE
 */
router.delete('/:id', userController.remove);

module.exports = router;
