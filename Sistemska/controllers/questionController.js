const session = require('express-session');
var QuestionModel = require('../models/questionModel.js');
const AnswersModel = require('../models/anwsersModel.js');
const UserModel = require('../models/userModel.js'); 

/**
 * questionController.js
 *
 * @description :: Server-side logic for managing questions.
 */
module.exports = {

    /**
     * questionController.list()
     */
    list: function (req, res) {
        QuestionModel.find(function (err, questions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting question.1',
                    error: err
                });
            }

            return res.json(questions);
        });
    },

    /**
     * questionController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        QuestionModel.findOne({_id: id}, function (err, question) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting question.2',
                    error: err
                });
            }

            if (!question) {
                return res.status(404).json({
                    message: 'No such question'
                });
            }

            return res.json(question);
        });
    },

    /**
     * questionController.create()
     */
    create: async function (req, res) {
        var question = new QuestionModel({
			questionText : req.body.questionText,
			description : req.body.description,
            creator : req.session.userId,
            answers : []
        });

        var question = await question.save()
        const updatedUser = await UserModel.findByIdAndUpdate(
            question.creator, 
            { $inc: { questions: 1 } },
            { new: true }
        );
        return res.redirect('/questions/displayQuestions');
    },

    /**
     * questionController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        QuestionModel.findOne({_id: id}, function (err, question) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting question.3',
                    error: err
                });
            }

            if (!question) {
                return res.status(404).json({
                    message: 'No such question'
                });
            }

            question.question = req.body.question ? req.body.question : question.question;
			question.description = req.body.description ? req.body.description : question.description;
			question.created = req.body.created ? req.body.created : question.created;
			
            question.save(function (err, question) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating question.',
                        error: err
                    });
                }

                return res.json(question);
            });
        });
    },

    /**
     * questionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        QuestionModel.findById(id, function (err, question) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when finding the question.',
                    error: err
                });
            }

            if (!question) {
                // If no question is found, return a 404
                return res.status(404).json({message: 'No such question'});
            }

            // If question is found, remove all answers associated with this question
            AnswersModel.deleteMany({_id: { $in: question.answers }}, function (err) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when deleting answers.',
                        error: err
                    });
                }

                // After deleting answers, remove the question itself
                QuestionModel.findByIdAndRemove(id, function (err) {
                    if (err) {
                        return res.status(500).json({
                            message: 'Error when deleting the question.',
                            error: err
                        });
                    }
                    // Return a 204 No Content status to indicate successful deletion
                    return res.status(204).json();
                });
            });
        });
    },

    addQuestions: function (req, res) {
        res.render('question/add');
    },

    displayQuestions: function (req, res) {
        QuestionModel.find({}, function(err, questions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting questions.',
                    error: err
                });
            }
            console.log(questions)
            return res.render('question/displayQuestions', { data: questions });
        });
    },

    displayQuestion: async function (req, res) {
        var id = req.params.id;
        const question = await QuestionModel.findByIdAndUpdate(
            id,
            { $push: { views: {viewedAt: new Date()} } },
            { new: true }
        ).exec();
        if (!question) {
            return res.status(404).json({message: 'No such question'});
        }

        const user = await UserModel.findById(question.creator).exec();
        if (!user) {
            return res.status(404).json({message: 'User not found'});
        }
        const profilePicture = user.profilePicture;  // Provide a default path if none exists
    

        let answerPromises = question.anwsers.map(answerId =>
            AnswersModel.findOne({_id: answerId}).exec().then(async answer => {
                if (answer) {
                    answer = answer.toObject();
                    answer.canDelete = (answer.creator.toString() === req.session.userId.toString());

                    answer.created = answer.created.toLocaleString('en-US', {
                        weekday: 'long', 
                        year: 'numeric', 
                        month: 'long', 
                        day: 'numeric', 
                        hour: '2-digit',
                        minute: '2-digit', 
                        second: '2-digit', 
                        hour12: true
                    });
                    const answerUser = await UserModel.findById(answer.creator._id).exec();
                    answer.profilePicture = answerUser.profilePicture;
                }
                return answer;
            })
        );

        let answers = await Promise.all(answerPromises);
        answers.sort((a, b) => {
            if (a.approved === b.approved) {
                // If both have the same approved status, sort by date (newest first)
                return b.created - a.created;
            }
            // Otherwise sort by approved status (true first)
            return a.approved ? -1 : 1;
        });
        var created= question.created.toLocaleString('en-US', {
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric', 
            hour: '2-digit',
            minute: '2-digit', 
            second: '2-digit', 
            hour12: true
        });
        const canApprove = (req.session.userId === question.creator.toString());
        // Now, we have all the answers resolved and stored in `answers`
        return res.render('question/displayQuestion', { 
            question: question, 
            answers: answers, 
            approve: canApprove, 
            created: created,
            profilePicture: profilePicture
        });
    },
    displayPopularQuestions: function (req, res) {
        const now = new Date();
        const timeInterval = new Date(now.getTime() - (24 * 60 * 60 * 1000));  // Define the 24-hour time interval
    
        QuestionModel.find({})
        .populate({ path: 'anwsers', model: 'anwsers' })
        .exec()
        .then(questions => {
            return Promise.all(questions.map(question => {
                // First, filter out old views and save the question
                const recentViews = question.views.filter(view => view.viewedAt >= timeInterval);
                question.views = recentViews;  // Update the views array to only include recent views
                return question.save();  // Save the cleaned-up question document
            }));
        })
        .then(cleanedQuestions => {
            // After cleaning, calculate popularity and sort
            const questionsWithPopularity = cleanedQuestions.map(question => {
                const recentAnswers = question.anwsers.filter(answer => answer.created >= timeInterval);
                console.log(question.views.length, recentAnswers.length)
                const popularity = question.views.length + 2 * recentAnswers.length;
                return { ...question.toObject(), popularity };
            });
            var sortedQuestions = questionsWithPopularity.sort((a, b) => b.popularity - a.popularity);
            return res.render('question/displayPopulatQuestions', { data: sortedQuestions });
        })
        .catch(err => {
            console.log("Error processing questions:", err);
        });
    }
};