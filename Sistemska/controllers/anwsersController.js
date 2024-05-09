var AnwsersModel = require('../models/anwsersModel.js');
var QuestionModel = require('../models/questionModel.js');
const UserModel = require('../models/userModel.js'); 

/**
 * anwsersController.js
 *
 * @description :: Server-side logic for managing anwserss.
 */
module.exports = {

    /**
     * anwsersController.list()
     */
    list: function (req, res) {
        AnwsersModel.find(function (err, anwserss) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting anwsers.',
                    error: err
                });
            }

            return res.json(anwserss);
        });
    },

    /**
     * anwsersController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        AnwsersModel.findOne({_id: id}, function (err, anwsers) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting anwsers.',
                    error: err
                });
            }
            if (!anwsers) {
                return res.status(404).json({
                    message: 'No such anwsers'
                });
            }

            return res.json(anwsers);
        });
    },

    /**
     * anwsersController.create()
     */
    create: function (req, res) {
        var anwsers = new AnwsersModel({
			anwserText : req.body.anwserText,
			creator : req.body.creator
        });

        anwsers.save(function (err, anwsers) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating anwsers',
                    error: err
                });
            }

            return res.status(201).json(anwsers);
        });
    },

    /**
     * anwsersController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        AnwsersModel.findOne({_id: id}, function (err, anwsers) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting anwsers',
                    error: err
                });
            }

            if (!anwsers) {
                return res.status(404).json({
                    message: 'No such anwsers'
                });
            }

            anwsers.anwserText = req.body.anwserText ? req.body.anwserText : anwsers.anwserText;
			anwsers.upvotes = req.body.upvotes ? req.body.upvotes : anwsers.upvotes;
			anwsers.downvotes = req.body.downvotes ? req.body.downvotes : anwsers.downvotes;
			anwsers.created = req.body.created ? req.body.created : anwsers.created;
			anwsers.creator = req.body.creator ? req.body.creator : anwsers.creator;
			
            anwsers.save(function (err, anwsers) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating anwsers.',
                        error: err
                    });
                }

                return res.json(anwsers);
            });
        });
    },

    /**
     * anwsersController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        AnwsersModel.findByIdAndRemove(id, function (err, anwsers) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the anwsers.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    },

    addAnwser: function(req, res){
        var anwser = new AnwsersModel({
			anwserText : req.body.anwserText,
			creator : req.session.userId
        });
        QuestionModel.findById(req.body.questionId, function(err, question) {
            if (err) {
                return res.status(500).send(err);
            }
            anwser.save(function(err, savedAnswer) {
                if (err) {
                    return res.status(500).send(err);
                }
                question.anwsers.push(savedAnswer._id);
                question.save(function(err) {
                    UserModel.findByIdAndUpdate(
                        req.session.userId,
                        { $inc: { anwsers: 1 } }, 
                        { new: true },
                        function(err, updatedUser) {
                            if (err) {
                                return res.status(500).send(err);
                            }
                            if (!updatedUser) {
                                return res.status(404).send("User not found");
                            }
        
                            res.redirect('/questions/displayQuestion/'+req.body.questionId);
                        }
                    );
                });
            });
        });
    },
    upvote: async function(req, res){
        try {
            const updatedAnswer = await AnwsersModel.findByIdAndUpdate(
                req.body.id, 
                { $inc: { upvotes: 1 } }, 
                { new: true }
            );
            await UserModel.findByIdAndUpdate(
                updatedAnswer.creator, 
                { $inc: { upvotes: 1 } }, 
                { new: true }
            );
            res.json(updatedAnswer);
        } catch (err) {
            res.status(500).json({message: "Failed to upvote", error: err});
        }
    },
    downvote: async function(req, res){
        console.log(req.body.id)
        try {
            const updatedAnswer = await AnwsersModel.findByIdAndUpdate(
                req.body.id, 
                { $inc: { downvotes: 1 } },  
                { new: true }  
            );
            await UserModel.findByIdAndUpdate(
                updatedAnswer.creator, 
                { $inc: { downvotes: 1 } }, 
                { new: true }
            );
            res.json(updatedAnswer);
        } catch (err) {
            console.log(err)
            res.status(500).json({message: "Failed to downvote", error: err});
        }
    },
    approve: function(req, res){
        const answerId = req.body.id; 

        AnwsersModel.findByIdAndUpdate(answerId, {$set: {approved: true}}, {new: true})
        .then(updatedAnswer => {
            if (!updatedAnswer) {
                res.status(404).send({message: "Answer not found"});
                return;
            }
            UserModel.findByIdAndUpdate(
                req.session.userId,
                { $inc: { approvedAnwsers: 1 } },  
                { new: true },
                function(err, updatedUser) {
                    if (err) {
                        return res.status(500).send(err);
                    }
                    if (!updatedUser) {
                        return res.status(404).send("User not found");
                    }

                    res.json(updatedAnswer);
                }
            );            
        })
        .catch(err => {
            res.status(500).json({message: "Failed to approve answer", error: err});
        });
    },
    dissapprove: function(req, res){
        const answerId = req.body.id;  

        AnwsersModel.findByIdAndUpdate(answerId, {$set: {approved: false}}, {new: true})
        .then(updatedAnswer => {
            if (!updatedAnswer) {
                res.status(404).send({message: "Answer not found"});
                return;
            }
            UserModel.findByIdAndUpdate(
                req.session.userId,
                { $inc: { approvedAnwsers:-1 } },  
                { new: true },
                function(err, updatedUser) {
                    if (err) {
                        return res.status(500).send(err);
                    }
                    if (!updatedUser) {
                        return res.status(404).send("User not found");
                    }

                    res.json(updatedAnswer);
                }
            );     
        })
        .catch(err => {
            res.status(500).json({message: "Failed to approve answer", error: err});
        });
    },
    deleteA: function(req, res){
        const answerId = req.params.id; 
        QuestionModel.findOneAndUpdate({anwsers: answerId}, {$pull: {anwsers: answerId}}, {new: true}).exec()
        .then(question => {
            if (!question) {
                return res.status(404).json({message: 'Question containing the answer not found.'});
            }
            return AnwsersModel.findByIdAndRemove(answerId).exec();
        })
        .then(answer => {
            if (!answer) {
                return res.status(404).json({message: 'Answer not found.'});
            }
            res.json({message: 'Answer deleted successfully.'});
        })
        .catch(err => {
            res.status(500).json({message: 'Error deleting answer.', error: err});
        });
    }
};
