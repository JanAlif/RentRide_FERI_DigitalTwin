var AnswerModel = require('../models/AnswerModel.js');
var QuestionModel = require('../models/QuestionModel.js');
var CommentModel = require('../models/CommentModel.js');

/**
 * AnswerController.js
 *
 * @description :: Server-side logic for managing Answers.
 */
module.exports = {

    /**
     * AnswerController.list()
     */
    list: function (req, res) {
        AnswerModel.find(function (err, Answers) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Answer.',
                    error: err
                });
            }

            return res.json(Answers);
        });
    },

    /**
     * AnswerController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        AnswerModel.findOne({ _id: id }, function (err, Answer) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Answer.',
                    error: err
                });
            }

            if (!Answer) {
                return res.status(404).json({
                    message: 'No such Answer'
                });
            }

            return res.json(Answer);
        });
    },

    /**
     * AnswerController.create()
     */
    create: function (req, res) {
        var Answer = new AnswerModel({
            questionId: req.body.questionId,
            content: req.body.content,
            postedBy: req.body.postedBy,
            postedDate: req.body.postedDate,
            upvote: req.body.upvote,
            downwote: req.body.downwote,
            isAccepted: req.body.isAccepted
        });

        Answer.save(function (err, Answer) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating Answer',
                    error: err
                });
            }

            return res.status(201).json(Answer);
        });
    },

    /**
     * AnswerController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        AnswerModel.findOne({ _id: id }, function (err, Answer) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Answer',
                    error: err
                });
            }

            if (!Answer) {
                return res.status(404).json({
                    message: 'No such Answer'
                });
            }

            Answer.questionId = req.body.questionId ? req.body.questionId : Answer.questionId;
            Answer.content = req.body.content ? req.body.content : Answer.content;
            Answer.postedBy = req.body.postedBy ? req.body.postedBy : Answer.postedBy;
            Answer.postedDate = req.body.postedDate ? req.body.postedDate : Answer.postedDate;
            Answer.upvote = req.body.upvote ? req.body.upvote : Answer.upvote;
            Answer.downwote = req.body.downwote ? req.body.downwote : Answer.downwote;
            Answer.isAccepted = req.body.isAccepted ? req.body.isAccepted : Answer.isAccepted;

            Answer.save(function (err, Answer) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating Answer.',
                        error: err
                    });
                }

                return res.json(Answer);
            });
        });
    },

    /**
     * AnswerController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;
    
        // First find the answer to get the questionId
        AnswerModel.findById(id, function (findErr, answer) {
            if (findErr || !answer) {
                return res.status(500).json({
                    message: 'Error when finding the Answer.',
                    error: findErr
                });
            }
    
            var questionId = answer.questionId; // Get the questionId from the answer
    
            // Delete all comments associated with the answer
            CommentModel.deleteMany({ answerId: id }, function (err) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when deleting comments related to the answer.',
                        error: err
                    });
                }
        
                // Then delete the answer
                AnswerModel.findByIdAndRemove(id, function (err) {
                    if (err) {
                        return res.status(500).json({
                            message: 'Error when deleting the Answer.',
                            error: err
                        });
                    }
                    res.redirect('/question/activityQuestionDetail/' + questionId); // Redirect using the retrieved questionId
                });
            });
        });
    },

    postAnswer: function (req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }

        var answer = new AnswerModel({
            questionId: req.body.questionId,
            content: req.body.answerContent,
            postedBy: req.session.userId,
            postedDate: new Date(),
            upvote: 0,
            downvote: 0,
            isAccepted: false
        });

        answer.save(function (err, savedAnswer) {
            if (err) {
                console.error('Error posting answer:', err);
                return res.status(500).send("Error occurred while posting the answer.");
            }

            // Update the corresponding question
            QuestionModel.findByIdAndUpdate(
                req.body.questionId,
                { $push: { answers: savedAnswer._id } }, // Push the new answer's ID
                { new: true, safe: true, upsert: true },
                function (questionErr, updatedQuestion) {
                    if (questionErr) {
                        console.error('Error updating question with new answer:', questionErr);
                        return res.status(500).send("Error occurred while updating the question.");
                    }

                    return res.redirect('/question/questionDetail/' + req.body.questionId);
                }
            );
        });
    },

    postAnswerMy: function (req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }

        var answer = new AnswerModel({
            questionId: req.body.questionId,
            content: req.body.answerContent,
            postedBy: req.session.userId,
            postedDate: new Date(),
            upvote: 0,
            downvote: 0,
            isAccepted: false
        });

        answer.save(function (err, savedAnswer) {
            if (err) {
                console.error('Error posting answer:', err);
                return res.status(500).send("Error occurred while posting the answer.");
            }

            // Update the corresponding question
            QuestionModel.findByIdAndUpdate(
                req.body.questionId,
                { $push: { answers: savedAnswer._id } }, // Push the new answer's ID
                { new: true, safe: true, upsert: true },
                function (questionErr, updatedQuestion) {
                    if (questionErr) {
                        console.error('Error updating question with new answer:', questionErr);
                        return res.status(500).send("Error occurred while updating the question.");
                    }

                    return res.redirect('/question/activityQuestionDetail/' + req.body.questionId);
                }
            );
        });
    },

    upvoteAnswer: function (req, res) {
        const answerId = req.params.answerId;
        const userId = req.session.userId;

        AnswerModel.findById(answerId, function (err, answer) {
            if (err || !answer) {
                return res.status(404).send('Answer not found');
            }

            // Check if the user has already upvoted
            const index = answer.upvotes.indexOf(userId);
            if (index > -1) {
                // User has already upvoted, remove the upvote
                answer.upvotes.splice(index, 1);
            } else {
                // Add user to upvotes and remove from downvotes
                answer.upvotes.push(userId);
                const downvoteIndex = answer.downvotes.indexOf(userId);
                if (downvoteIndex > -1) {
                    answer.downvotes.splice(downvoteIndex, 1);
                }
            }

            answer.save(function (err) {
                if (err) {
                    return res.status(500).send('Error saving answer');
                }
                res.redirect('/question/questionDetail/' + answer.questionId);
            });
        });
    },

    upvoteAnswerMy: function (req, res) {
        const answerId = req.params.answerId;
        const userId = req.session.userId;

        AnswerModel.findById(answerId, function (err, answer) {
            if (err || !answer) {
                return res.status(404).send('Answer not found');
            }

            // Check if the user has already upvoted
            const index = answer.upvotes.indexOf(userId);
            if (index > -1) {
                // User has already upvoted, remove the upvote
                answer.upvotes.splice(index, 1);
            } else {
                // Add user to upvotes and remove from downvotes
                answer.upvotes.push(userId);
                const downvoteIndex = answer.downvotes.indexOf(userId);
                if (downvoteIndex > -1) {
                    answer.downvotes.splice(downvoteIndex, 1);
                }
            }

            answer.save(function (err) {
                if (err) {
                    return res.status(500).send('Error saving answer');
                }
                res.redirect('/question/ActivityQuestionDetail/' + answer.questionId);
            });
        });
    },

    downvoteAnswer: function (req, res) {
        const answerId = req.params.answerId;
        const userId = req.session.userId;

        AnswerModel.findById(answerId, function (err, answer) {
            if (err || !answer) {
                return res.status(404).send('Answer not found');
            }

            // Check if the user has already downvoted
            const index = answer.downvotes.indexOf(userId);
            if (index > -1) {
                // User has already downvoted, remove the downvote
                answer.downvotes.splice(index, 1);
            } else {
                // Add user to downvotes and remove from upvotes
                answer.downvotes.push(userId);
                const upvoteIndex = answer.upvotes.indexOf(userId);
                if (upvoteIndex > -1) {
                    answer.upvotes.splice(upvoteIndex, 1);
                }
            }

            answer.save(function (err) {
                if (err) {
                    return res.status(500).send('Error saving answer');
                }
                res.redirect('/question/questionDetail/' + answer.questionId);
            });
        });
    },

    downvoteAnswerMy: function (req, res) {
        const answerId = req.params.answerId;
        const userId = req.session.userId;

        AnswerModel.findById(answerId, function (err, answer) {
            if (err || !answer) {
                return res.status(404).send('Answer not found');
            }

            // Check if the user has already downvoted
            const index = answer.downvotes.indexOf(userId);
            if (index > -1) {
                // User has already downvoted, remove the downvote
                answer.downvotes.splice(index, 1);
            } else {
                // Add user to downvotes and remove from upvotes
                answer.downvotes.push(userId);
                const upvoteIndex = answer.upvotes.indexOf(userId);
                if (upvoteIndex > -1) {
                    answer.upvotes.splice(upvoteIndex, 1);
                }
            }

            answer.save(function (err) {
                if (err) {
                    return res.status(500).send('Error saving answer');
                }
                res.redirect('/question/activityQuestionDetail/' + answer.questionId);
            });
        });
    },

    markAsCorrect: function(req, res) {
        const answerId = req.params.answerId;
        const questionId = req.body.questionId;
    
        // Find the question and all its answers
        QuestionModel.findById(questionId).populate('answers').exec(function(err, question) {
            if (err || !question) {
                // handle error or "question not found"
                return res.status(500).send("Error updating the answer");
            }
    
            // Reset 'isAccepted' for all answers
            question.answers.forEach(answer => {
                AnswerModel.findByIdAndUpdate(answer._id, { $set: { isAccepted: false } }, err => {
                    if (err) {
                        // handle error
                        return res.status(500).send("Error updating the answer"); //TUKAJJJJJJJJJ
                    }
                });
            });
    
            // Set 'isAccepted' to true for the selected answer
            AnswerModel.findByIdAndUpdate(answerId, { $set: { isAccepted: true } }, function(err) {
                if (err) {
                    // handle error
                    return res.status(500).send("Error updating the answer");
                }
                res.redirect('/question/activityQuestionDetail/' + questionId);
            });
        });
    }
};
