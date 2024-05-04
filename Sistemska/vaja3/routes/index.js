var express = require('express');
var router = express.Router();
var UserModel = require('../models/userModel.js'); // You need to require the UserModel
var PhotosModel = require('../models/photosModel.js'); // Require the PhotosModel
var userController = require('../controllers/userController.js');
var QuestionModel = require('../models/QuestionModel.js');

/* GET home page. */
router.get('/', function(req, res, next) {
  // Get all questions from the database
  QuestionModel.find({})
    .populate('postedBy', 'username') 
    .sort({ postedDate: -1 })
    // .sort({ _id: -1 })
    .exec(function(questionError, questions) {
      if (questionError) {
        return next(questionError);
      }

      if (req.session && req.session.userId) {
        // Fetch both user and photo details if user is logged in
        UserModel.findById(req.session.userId, function(userError, user) {
          if (userError) {
            return next(userError);
          }

          PhotosModel.findOne({ usedBy: req.session.userId }, function(photoError, photo) {
            let photoPath = photo ? photo.path : '/images/default.jpg';
            if (photoError) {
              console.error('Error fetching photo:', photoError);
            }

            // Render the index page with user, photo, and questions data
            res.render('index', {
              title: 'Express',
              user: user,
              photoPath: photoPath,
              questions: questions
            });
          });
        });
      } else {
        // Render index with questions but without user details if not logged in
        res.render('index', {
          title: 'Express',
          user: null,
          photoPath: '/images/default.jpg',
          questions: questions,
        });
      }
    });
});

// Route to handle user login
router.post('/login', userController.login);

// Route to display user profile - must be authenticated
router.get('/user/profile', userController.profile);

// Route to handle user logout
router.get('/logout', userController.logout);

module.exports = router;
