var UserModel = require('../models/userModel.js');
var PhotosModel = require('../models/photosModel.js');
var QuestionModel = require('../models/QuestionModel.js');
var AnswerModel = require('../models/AnswerModel.js');
var CommentModel = require('../models/CommentModel.js');
const multer = require('multer');
const path = require('path');

const storage = multer.diskStorage({
    destination: function(req, file, cb) {
        cb(null, 'public/images/');
    },
    filename: function(req, file, cb) {
        cb(null, req.session.userId + '-' + Date.now() + path.extname(file.originalname));
    }
});
const upload = multer({ storage: storage });

/**
 * userController.js
 *
 * @description :: Server-side logic for managing users.
 */
module.exports = {

    /**
     * userController.list()
     */
    list: function (req, res) {
        UserModel.find(function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting user.',
                    error: err
                });
            }

            return res.json(users);
        });
    },

    /**
     * userController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        UserModel.findOne({ _id: id }, function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting user.',
                    error: err
                });
            }

            if (!user) {
                return res.status(404).json({
                    message: 'No such user'
                });
            }

            return res.json(user);
        });
    },

    /**
     * userController.create()
     */
    create: function (req, res) {
        UserModel.findOne({ username: req.body.username }, function (err, existingUser) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when checking for existing user',
                    error: err
                });
            }

            if (existingUser) {
                // User already exists with this username
                return res.status(409).send('Username already taken');
            }

            var user = new UserModel({
                username: req.body.username,
                email: req.body.email,
                password: req.body.password
            });

            user.save(function (err, user) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when creating user',
                        error: err
                    });
                }

                // Assuming you want to log the user in immediately after registration
                req.session.userId = user._id;
                return res.redirect('/user/profile');
            });
        });
    },


    /**
     * userController.update()
     */
    update: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }
    
        UserModel.findById(req.session.userId, function(err, currentUser) {
            if (err) {
                console.error('Error on the server:', err);
                return res.status(500).send('Error on the server.');
            }
    
            if (!currentUser) {
                return res.status(404).send('User not found.');
            }
    
            const renderWithError = (errorMessage) => {
                PhotosModel.findOne({ usedBy: req.session.userId }, function(photoErr, photo) {
                    let photoPath = '/images/default.jpg'; // Default photo path
                    if (!photoErr && photo) {
                        photoPath = photo.path; // Path to the user's photo
                    }
    
                    res.render('user/editProfile', {
                        errorMessage: errorMessage,
                        username: currentUser.username,
                        email: currentUser.email,
                        photoPath: photoPath
                    });
                });
            };
    
            if (req.body.username && req.body.username !== currentUser.username) {
                UserModel.findOne({ username: req.body.username }, function(err, existingUser) {
                    if (err) {
                        console.error('Error on the server:', err);
                        return renderWithError('Error on the server.');
                    }
    
                    if (existingUser) {
                        return renderWithError('Username already taken.');
                    }
    
                    currentUser.username = req.body.username;
                    updateUser(currentUser, req, res);
                });
            } else {
                updateUser(currentUser, req, res);
            }
        });
    },

    /**
     * userController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        UserModel.findByIdAndRemove(id, function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the user.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    },

    showLogin: function (req, res) {
        const loginFailed = req.query.loginFailed;
        res.render('user/login', { loginFailed: loginFailed });
    },
    showRegister: function (req, res) {
        res.render('user/register');
    },
    showIndex: function (req, res) {
        res.render('/');
    },
    showEditProfile: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }
    
        UserModel.findById(req.session.userId, function(err, user) {
            if (err) {
                console.error('Error fetching user:', err);
                return res.status(500).send('Internal Server Error');
            }
    
            if (!user) {
                return res.status(404).send('User not found');
            }
    
            PhotosModel.findOne({ usedBy: req.session.userId }, function(photoErr, photo) {
                let photoPath = '/images/default.jpg'; // Default photo path
                if (photoErr) {
                    console.error('Error fetching photo:', photoErr);
                    // Optionally handle the error or log it
                } else if (photo) {
                    photoPath = photo.path; // Path to the user's photo
                }
    
                res.render('user/editProfile', {
                    username: user.username,
                    email: user.email,
                    photoPath: photoPath
                });
            });
        });
    },
    

    login: function (req, res, next) {
        //console.log(req.body);
        UserModel.authenticate(req.body.username, req.body.password, function (error, user) {
            if (error) {
                var err = new Error('An error occurred during the login process.');
                err.status = 500;
                return next(err);
            }
            if (!user) {
                var err = new Error("wrong username or password");
                err.status = 401;
                // Optionally, redirect back to the login page with an error message
                return res.redirect('/user/login?loginFailed=true');
            }
            req.session.userId = user._id;
            return res.redirect('/'); // Adjusted redirect path
        });
    },


    profile: function(req, res, next) {
        UserModel.findById(req.session.userId)
            .exec(async function(error, user) {
                if (error) {
                    return next(error);
                } else {
                    if (user == null) {
                        var err = new Error("Not authorized");
                        err.status = 400;
                        return next(err);
                    } else {
                        try {
                            let photo = await PhotosModel.findOne({ usedBy: req.session.userId });
                            let photoPath = photo ? photo.path : '/images/default.jpg';
    
                            let questionCount = await QuestionModel.countDocuments({ postedBy: req.session.userId });
                            let answerCount = await AnswerModel.countDocuments({ postedBy: req.session.userId });
                            let commentCount = await CommentModel.countDocuments({ postedBy: req.session.userId });
    
                            let answers = await AnswerModel.find({ postedBy: req.session.userId });
                            let totalUpvotes = answers.reduce((acc, answer) => acc + answer.upvotes.length, 0);
                            let totalDownvotes = answers.reduce((acc, answer) => acc + answer.downvotes.length, 0);
    
                            let successMessage = req.query.passwordUpdated ? 'Password updated successfully.' : '';
    
                            res.render('user/profile', {
                                username: user.username,
                                email: user.email,
                                photoPath: photoPath,
                                successMessage: successMessage,
                                questionCount,
                                answerCount,
                                commentCount,
                                totalUpvotes,
                                totalDownvotes
                            });
                        } catch (err) {
                            console.error('Error fetching user data:', err);
                            res.status(500).send('Error when getting user data');
                        }
                    }
                }
            });
    },

    logout: function (req, res, next) {
        if (req.session) {
            req.session.destroy(function (err) {
                if (err) {
                    return next(err);
                } else {
                    return res.redirect('/');
                }
            });
        }
    },

    editProfile: function (userId, newUsername, callback) {
        UserModel.findById(userId, function (err, user) {
            if (err) {
                callback(err);
            } else if (user) {
                user.username = newUsername;
                user.save(function (err) {
                    callback(err);
                });
            } else {
                callback(new Error('User not found'));
            }
        });
    },

    updatePassword: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }
    
        UserModel.findById(req.session.userId, function(err, user) {
            if (err) {
                console.error('Error fetching user:', err);
                return res.status(500).send('Internal Server Error');
            }
    
            if (!user) {
                return res.status(404).send('User not found');
            }
    
            const renderWithError = (errorMessagePswd) => {
                PhotosModel.findOne({ usedBy: req.session.userId }, function(photoErr, photo) {
                    let photoPath = '/images/default.jpg'; // Default photo path
                    if (!photoErr && photo) {
                        photoPath = photo.path; // Path to the user's photo
                    }
    
                    res.render('user/editProfile', {
                        errorMessagePswd: errorMessagePswd,
                        username: user.username,
                        email: user.email,
                        photoPath: photoPath
                    });
                });
            };
    
            UserModel.authenticate(user.username, req.body.oldPassword, function(authErr, isMatch) {
                if (authErr || !isMatch) {
                    return renderWithError('Old password is incorrect');
                }
    
                if (req.body.newPassword !== req.body.newPasswordRepeat) {
                    return renderWithError('New passwords do not match');
                }
    
                user.updatePassword(req.body.newPassword, function(updateErr) {
                    if (updateErr) {
                        console.error('Error updating password:', updateErr);
                        return renderWithError('Error updating password');
                    }
                    return res.redirect('/user/profile?passwordUpdated=true');
                });
            });
        });
    },
    

    updatePhoto: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }
    
        upload.single('photo')(req, res, function(err) {
            if (err) {
                console.error("Multer upload error:", err);
                return res.status(500).render('user/editProfile', {
                    errorMessage: 'Error uploading file.',
                    username: req.session.username,
                    email: req.session.email
                });
            }
    
            if (!req.file) {
                console.error("No file uploaded.");
                return res.status(500).render('user/editProfile', {
                    errorMessage: 'No file uploaded.',
                    username: req.session.username,
                    email: req.session.email
                });
            }
    
            const photoPath = '/images/' + req.file.filename;
    
            PhotosModel.findOneAndUpdate(
                { usedBy: req.session.userId }, 
                { path: photoPath }, 
                { upsert: true, new: true, setDefaultsOnInsert: true },
                function(err, result) {
                    if (err) {
                        console.error("Error updating or creating photo:", err);
                        return res.status(500).send('Error updating photo: ' + err.message);
                    }
                    res.redirect('/user/profile');
                }
            );
        });
    },

    showMyActivity: function (req, res) {
        let userId = req.session.userId;
    
        // Find questions posted by the user
        QuestionModel.find({ postedBy: userId }).populate('postedBy').exec()
        .then(postedQuestions => {
            // Find answers posted by the user and the corresponding questions
            return AnswerModel.find({ postedBy: userId }).populate('questionId').exec()
            .then(userAnswers => {
                let answeredQuestionIds = userAnswers.map(answer => answer.questionId);
                return QuestionModel.find({ _id: { $in: answeredQuestionIds } }).populate('postedBy').exec();
            })
            .then(answeredQuestions => {
                // Combine posted questions and answered questions
                let combinedQuestions = [...postedQuestions, ...answeredQuestions];
    
                // Eliminate duplicate questions
                let uniqueQuestions = Array.from(new Set(combinedQuestions.map(a => a.id)))
                                          .map(id => {
                                              return combinedQuestions.find(a => a.id === id)
                                          });
    
                // Render the myActivity page with unique questions
                res.render('user/myActivity', {
                    questions: uniqueQuestions
                });
            });
        })
        .catch(err => {
            console.error('Error fetching user activity:', err);
            res.status(500).send('Error when getting user activity');
        });
    }
    
};

function updateUser(currentUser, req, res) {
    // Update other fields
    if (req.body.email) currentUser.email = req.body.email;
    // Add other fields as needed

    currentUser.save(function (err) {
        if (err) {
            // handle error
            return res.status(500).send('Error updating profile.');
        }
        return res.redirect('/user/profile'); // or wherever you want to redirect after the update
    });
}