var QuestionModel = require('../models/QuestionModel.js');
var PhotosModel = require('../models/photosModel.js');
var AnswerModel = require('../models/AnswerModel.js');
var CommentModel = require('../models/CommentModel.js');
var UserModel = require('../models/userModel.js');

/**
 * QuestionController.js
 *
 * @description :: Server-side logic for managing Questions.
 */
module.exports = {

    /**
     * QuestionController.list()
     */
    list: function (req, res) {
        QuestionModel.find(function (err, Questions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Question.',
                    error: err
                });
            }

            return res.json(Questions);
        });
    },

    /**
     * QuestionController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        QuestionModel.findOne({ _id: id }, function (err, Question) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Question.',
                    error: err
                });
            }

            if (!Question) {
                return res.status(404).json({
                    message: 'No such Question'
                });
            }

            return res.json(Question);
        });
    },

    /**
     * QuestionController.create()
     */
    create: function (req, res) {
        var Question = new QuestionModel({
            title: req.body.title,
            description: req.body.description,
            postedBy: req.body.postedBy,
            postedDate: req.body.postedDate,
            viewsCount: req.body.viewsCount,
            answers: req.body.answers,
            acceptedAnswer: req.body.acceptedAnswer
        });

        Question.save(function (err, Question) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating Question',
                    error: err
                });
            }

            return res.status(201).json(Question);
        });
    },

    /**
     * QuestionController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        QuestionModel.findOne({ _id: id }, function (err, Question) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Question',
                    error: err
                });
            }

            if (!Question) {
                return res.status(404).json({
                    message: 'No such Question'
                });
            }

            Question.title = req.body.title ? req.body.title : Question.title;
            Question.description = req.body.description ? req.body.description : Question.description;
            Question.postedBy = req.body.postedBy ? req.body.postedBy : Question.postedBy;
            Question.postedDate = req.body.postedDate ? req.body.postedDate : Question.postedDate;
            Question.viewsCount = req.body.viewsCount ? req.body.viewsCount : Question.viewsCount;
            Question.answers = req.body.answers ? req.body.answers : Question.answers;
            Question.acceptedAnswer = req.body.acceptedAnswer ? req.body.acceptedAnswer : Question.acceptedAnswer;

            Question.save(function (err, Question) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating Question.',
                        error: err
                    });
                }

                return res.json(Question);
            });
        });
    },

    /**
     * QuestionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;
    
        // Step 1: Delete all comments associated with the question
        CommentModel.deleteMany({ questionId: id }, function (commentErr) {
            if (commentErr) {
                return res.status(500).json({
                    message: 'Error when deleting comments.',
                    error: commentErr
                });
            }
    
            // Step 2: Delete all answers associated with the question
            AnswerModel.deleteMany({ questionId: id }, function (answerErr) {
                if (answerErr) {
                    return res.status(500).json({
                        message: 'Error when deleting answers.',
                        error: answerErr
                    });
                }
    
                // Step 3: Delete the question itself
                QuestionModel.findByIdAndRemove(id, function (questionErr) {
                    if (questionErr) {
                        return res.status(500).json({
                            message: 'Error when deleting the Question.',
                            error: questionErr
                        });
                    }
    
                    res.redirect('/user/profile'); // Redirect to a safe page after deletion
                });
            });
        });
    },

    showPostQuestion: function (req, res) {
        res.render('question/postQuestion');
    },

    incrementViewsAndShowDetail: function (req, res, next) {
        var questionId = req.params.id;

        // Increment the view count
        QuestionModel.findByIdAndUpdate(
            questionId,
            { $inc: { viewsCount: 1 } },
            { new: true },
            function (error) {
                if (error) {
                    return next(error);
                }
                // Now call showQuestionDetail
                this.showQuestionDetail(req, res, next);
            }.bind(this) // Bind 'this' to use showQuestionDetail function
        );
    },

    showQuestionDetail: function (req, res) {
        var questionId = req.params.id;

        QuestionModel.findById(questionId)
            .populate('postedBy')
            .populate({
                path: 'answers',
                populate: {
                    path: 'postedBy',
                    model: 'user',
                    select: 'username'
                },
                options: { sort: { 'postedDate': -1 } }
            })
            .then(question => {
                if (!question) {
                    throw new Error('Question not found');
                }

                question.answers.sort((a, b) => {
                    if (a.isAccepted) return -1;
                    if (b.isAccepted) return 1;
                    return b.postedDate - a.postedDate;
                });

                // Fetch comments for the question (no answerId)
                return CommentModel.find({ questionId: question._id, answerId: { $exists: false } })
                    .populate('postedBy', 'user')
                    .then(questionComments => {
                        // Fetch comments for each answer
                        let answerCommentsPromises = question.answers.map(answer => {
                            return CommentModel.find({ answerId: answer._id })
                                .populate('postedBy', 'user');
                        });

                        return Promise.all(answerCommentsPromises)
                            .then(answerComments => {
                                return { question, questionComments, answerComments };
                            });
                    });
            })
            .then(data => {
                let { question, questionComments, answerComments } = data;

                let photoPromises = [];

                questionComments.forEach(comment => {
                    photoPromises.push(
                        PhotosModel.findOne({ usedBy: comment.postedBy._id })
                            .then(photo => {
                                comment.postedBy.profilePicURL = photo ? photo.path : '/images/default.jpg';
                            })
                    );
                });

                question.answers.forEach((answer, index) => {
                    photoPromises.push(
                        PhotosModel.findOne({ usedBy: answer.postedBy._id })
                            .then(photo => {
                                answer.postedBy.profilePicURL = photo ? photo.path : '/images/default.jpg';
                            })
                    );

                    answerComments[index].forEach(comment => {
                        photoPromises.push(
                            PhotosModel.findOne({ usedBy: comment.postedBy._id })
                                .then(photo => {
                                    comment.postedBy.profilePicURL = photo ? photo.path : '/images/default.jpg';
                                })
                        );
                    });
                });

                return PhotosModel.findOne({ usedBy: question.postedBy._id })
                    .then(questionPosterPhoto => {
                        let questionPosterPicURL = questionPosterPhoto ? questionPosterPhoto.path : '/images/default.jpg';
                        return Promise.all(photoPromises).then(() => {
                            return { question, questionComments, answerComments, questionPosterPicURL };
                        });
                    });
            })
            .then(data => {
                let { question, questionComments, answerComments, questionPosterPicURL } = data;
                res.render('question/questionDetail', {
                    question: question,
                    questionPosterPicURL: questionPosterPicURL,
                    comments: questionComments,
                    answerComments: answerComments
                });
            })
            .catch(err => {
                console.error('Error in showQuestionDetail:', err);
                res.status(500).send('Error when getting question details');
            });
    },


    postQuestion: function (req, res) {
        if (!req.session.userId) {
            // Redirect the user to the login page if not logged in
            return res.redirect('/user/login');
        }

        var Question = new QuestionModel({
            title: req.body.title,
            description: req.body.description,
            postedBy: req.session.userId, 
            postedDate: new Date(), 
            viewsCount: 0, 
            answers: [], 
            
        });

        Question.save(function (err, question) {
            if (err) {
                console.error('Error when posting question:', err);
                return res.status(500).json({
                    message: 'Error when creating Question',
                    error: err
                });
            }

            // Redirect to the question's detail page or some other page as per your app's flow
            return res.redirect('/');
        });
    },

    showActivityQuestionDetail: function (req, res) {
        var questionId = req.params.id;

        QuestionModel.findById(questionId)
            .populate('postedBy')
            .populate({
                path: 'answers',
                populate: {
                    path: 'postedBy',
                    model: 'user',
                    select: 'username'
                },
                options: { sort: { 'postedDate': -1 } }
            })
            .then(question => {
                if (!question) {
                    throw new Error('Question not found');
                }

                question.answers.sort((a, b) => {
                    if (a.isAccepted) return -1;
                    if (b.isAccepted) return 1;
                    return b.postedDate - a.postedDate;
                });

                // Add authorship information to question and answers
                question.isUserAuthor = req.session.userId && question.postedBy._id.toString() === req.session.userId.toString();
                question.answers.forEach(answer => {
                    answer.isUserAuthor = req.session.userId && answer.postedBy._id.toString() === req.session.userId.toString();
                });

                // Fetch comments for the question (no answerId)
                return CommentModel.find({ questionId: question._id, answerId: { $exists: false } })
                .populate('postedBy', 'user')
                .then(questionComments => {
                    questionComments.forEach(comment => {
                        // Check if the current user is the author of the comment
                        comment.isUserAuthor = req.session.userId && comment.postedBy._id.toString() === req.session.userId.toString();
                    });
            
                    // Fetch comments for each answer
                    let answerCommentsPromises = question.answers.map(answer => {
                        return CommentModel.find({ answerId: answer._id })
                            .populate('postedBy', 'user')
                            .then(answerComments => {
                                // Similarly, set isUserAuthor for each answer comment
                                answerComments.forEach(comment => {
                                    comment.isUserAuthor = req.session.userId && comment.postedBy._id.toString() === req.session.userId.toString();
                                });
                                return answerComments;
                            });
                    });

                        return Promise.all(answerCommentsPromises)
                            .then(answerComments => {
                                return { question, questionComments, answerComments };
                            });
                    });
            })
            .then(data => {
                let { question, questionComments, answerComments } = data;

                let photoPromises = [];

                questionComments.forEach(comment => {
                    photoPromises.push(
                        PhotosModel.findOne({ usedBy: comment.postedBy._id })
                            .then(photo => {
                                comment.postedBy.profilePicURL = photo ? photo.path : '/images/default.jpg';
                            })
                    );
                });

                question.answers.forEach((answer, index) => {
                    photoPromises.push(
                        PhotosModel.findOne({ usedBy: answer.postedBy._id })
                            .then(photo => {
                                answer.postedBy.profilePicURL = photo ? photo.path : '/images/default.jpg';
                            })
                    );

                    answerComments[index].forEach(comment => {
                        photoPromises.push(
                            PhotosModel.findOne({ usedBy: comment.postedBy._id })
                                .then(photo => {
                                    comment.postedBy.profilePicURL = photo ? photo.path : '/images/default.jpg';
                                })
                        );
                    });
                });

                return PhotosModel.findOne({ usedBy: question.postedBy._id })
                    .then(questionPosterPhoto => {
                        let questionPosterPicURL = questionPosterPhoto ? questionPosterPhoto.path : '/images/default.jpg';
                        return Promise.all(photoPromises).then(() => {
                            return { question, questionComments, answerComments, questionPosterPicURL };
                        });
                    });
            })
            .then(data => {
                let { question, questionComments, answerComments, questionPosterPicURL } = data;
                let isAuthor = req.session.userId && question.postedBy &&
                    req.session.userId.toString() === question.postedBy._id.toString();
                //console.log('nekee ',answerComments.postedBy);
                //console.log('nekeeee ',questionComments.postedBy);
                //console.log('Question Posted By ID:', question.postedBy._id);
                //console.log('Is Author:', isAuthor);
                res.render('question/activityQuestionDetail', {
                    question: question,
                    questionPosterPicURL: questionPosterPicURL,
                    comments: questionComments,
                    answerComments: answerComments,
                    isAuthor: isAuthor,
                });
            })
            .catch(err => {
                console.error('Error in showQuestionDetail:', err);
                res.status(500).send('Error when getting question details');
            });
    },

    showHotQuestions: function (req, res) {
        const timeLimit = new Date();
        timeLimit.setDate(timeLimit.getDate() - 7); // Set the time frame, e.g., last 7 days
    
        QuestionModel.find({ postedDate: { $gte: timeLimit } })
            .populate('postedBy', 'username')
            .populate({
                path: 'answers',
                model: 'Answer' // Make sure this matches your Answer model name
            })
            .exec(function (err, questions) {
                if (err) {
                    console.error('Error fetching hot questions:', err);
                    return res.status(500).send('Error occurred while fetching questions.');
                }
    
                // Sort questions first by the number of answers, then by views
                questions.sort((a, b) => {
                    let comparison = b.answers.length - a.answers.length;
                    if (comparison === 0) {
                        comparison = b.viewsCount - a.viewsCount;
                    }
                    return comparison;
                });
    
                // Check if user is logged in
                if (req.session && req.session.userId) {
                    UserModel.findById(req.session.userId, function(userError, user) {
                        if (userError) {
                            return res.status(500).send('Error occurred while fetching user.');
                        }
    
                        PhotosModel.findOne({ usedBy: req.session.userId }, function(photoError, photo) {
                            let photoPath = photo ? photo.path : '/images/default.jpg';
                            res.render('question/hotQuestions', {
                                user: user,
                                photoPath: photoPath,
                                hotQuestions: questions
                            });
                        });
                    });
                } else {
                    // If user is not logged in
                    res.render('question/hotQuestions', {
                        user: null,
                        photoPath: '/images/default.jpg',
                        hotQuestions: questions
                    });
                }
            });
    }
    
    

};
