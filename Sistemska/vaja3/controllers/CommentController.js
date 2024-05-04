var CommentModel = require('../models/CommentModel.js');

/**
 * CommentController.js
 *
 * @description :: Server-side logic for managing Comments.
 */
module.exports = {

    /**
     * CommentController.list()
     */
    list: function (req, res) {
        CommentModel.find(function (err, Comments) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Comment.',
                    error: err
                });
            }

            return res.json(Comments);
        });
    },

    /**
     * CommentController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        CommentModel.findOne({_id: id}, function (err, Comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Comment.',
                    error: err
                });
            }

            if (!Comment) {
                return res.status(404).json({
                    message: 'No such Comment'
                });
            }

            return res.json(Comment);
        });
    },

    /**
     * CommentController.create()
     */
    create: function (req, res) {
        var Comment = new CommentModel({
			questionId : req.body.questionId,
			answerId : req.body.answerId,
			content : req.body.content,
			postedBy : req.body.postedBy,
			postedDate : req.body.postedDate
        });

        Comment.save(function (err, Comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating Comment',
                    error: err
                });
            }

            return res.status(201).json(Comment);
        });
    },

    /**
     * CommentController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        CommentModel.findOne({_id: id}, function (err, Comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting Comment',
                    error: err
                });
            }

            if (!Comment) {
                return res.status(404).json({
                    message: 'No such Comment'
                });
            }

            Comment.questionId = req.body.questionId ? req.body.questionId : Comment.questionId;
			Comment.answerId = req.body.answerId ? req.body.answerId : Comment.answerId;
			Comment.content = req.body.content ? req.body.content : Comment.content;
			Comment.postedBy = req.body.postedBy ? req.body.postedBy : Comment.postedBy;
			Comment.postedDate = req.body.postedDate ? req.body.postedDate : Comment.postedDate;
			
            Comment.save(function (err, Comment) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating Comment.',
                        error: err
                    });
                }

                return res.json(Comment);
            });
        });
    },

    /**
     * CommentController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        CommentModel.findByIdAndRemove(id, function (err, Comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the Comment.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    },

    postComment: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }

        var newComment = new CommentModel({
            questionId: req.body.questionId,
            answerId: req.body.answerId, // This can be undefined if the comment is for the question
            content: req.body.commentContent,
            postedBy: req.session.userId,
            postedDate: new Date()
        });

        newComment.save(function(err, comment) {
            if (err) {
                console.error('Error posting comment:', err);
                return res.status(500).send('Error occurred while posting the comment.');
            }

            // Redirect back to the question detail page
            let redirectTo = req.body.answerId ? '/question/questionDetail/' + req.body.questionId : '/';
            return res.redirect('/question/questionDetail/' + req.body.questionId);
        });
    },

    postCommentMy: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }

        var newComment = new CommentModel({
            questionId: req.body.questionId,
            answerId: req.body.answerId, // This can be undefined if the comment is for the question
            content: req.body.commentContent,
            postedBy: req.session.userId,
            postedDate: new Date()
        });

        newComment.save(function(err, comment) {
            if (err) {
                console.error('Error posting comment:', err);
                return res.status(500).send('Error occurred while posting the comment.');
            }

            // Redirect back to the question detail page
            let redirectTo = req.body.answerId ? '/question/questionDetail/' + req.body.questionId : '/';
            return res.redirect('/question/activityQuestionDetail/' + req.body.questionId);
        });
    },

    postAnswerComment: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }

        console.log(req.body);

        var newComment = new CommentModel({
            questionId: req.body.questionId,
            answerId: req.body.answerId, // This can be undefined if the comment is for the question
            content: req.body.commentContent,
            postedBy: req.session.userId,
            postedDate: new Date()
        });

        newComment.save(function(err, comment) {
            if (err) {
                console.error('Error posting comment:', err);
                return res.status(500).send('Error occurred while posting the comment.');
            }

            // Redirect back to the question detail page
            let redirectTo = req.body.answerId ? '/question/questionDetail/' + req.body.questionId : '/';
            return res.redirect('/question/questionDetail/' + req.body.questionId);
        });
    },

    postAnswerCommentMy: function(req, res) {
        if (!req.session.userId) {
            return res.redirect('/user/login');
        }

        console.log(req.body);

        var newComment = new CommentModel({
            questionId: req.body.questionId,
            answerId: req.body.answerId, // This can be undefined if the comment is for the question
            content: req.body.commentContent,
            postedBy: req.session.userId,
            postedDate: new Date()
        });

        newComment.save(function(err, comment) {
            if (err) {
                console.error('Error posting comment:', err);
                return res.status(500).send('Error occurred while posting the comment.');
            }

            // Redirect back to the question detail page
            let redirectTo = req.body.answerId ? '/question/questionDetail/' + req.body.questionId : '/';
            return res.redirect('/question/activityQuestionDetail/' + req.body.questionId);
        });
    }
};
