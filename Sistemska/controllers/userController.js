var UserModel = require('../models/userModel.js');
const multer = require('multer');

const storage = multer.diskStorage({
    destination: function(req, file, cb) {
        cb(null, './public/images');  // Save files to the './uploads/' directory
    },
    filename: function(req, file, cb) {
        // Use the original file name
        cb(null, file.originalname);
    }
});

const upload = multer({ storage: storage }).single('profilePic');

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

        UserModel.findOne({_id: id}, function (err, user) {
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
        var user = new UserModel({
			username : req.body.username,
			password : req.body.password,
			email : req.body.email
        });

        user.save(function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating user',
                    error: err
                });
            }

            //return res.status(201).json(user);
            return res.redirect('/users/login');
        });
    },

    /**
     * userController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        UserModel.findOne({_id: id}, function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting user',
                    error: err
                });
            }

            if (!user) {
                return res.status(404).json({
                    message: 'No such user'
                });
            }

            user.username = req.body.username ? req.body.username : user.username;
			user.password = req.body.password ? req.body.password : user.password;
			user.email = req.body.email ? req.body.email : user.email;
			
            user.save(function (err, user) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating user.',
                        error: err
                    });
                }

                return res.json(user);
            });
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

    showRegister: function(req, res){
        res.render('user/register');
    },

    showLogin: function(req, res){
        res.render('user/login');
    },

    login: function(req, res, next){
        UserModel.authenticate(req.body.username, req.body.password, function(err, user){
            if(err || !user){
                var err = new Error('Wrong username or paassword');
                err.status = 401;
                return next(err);
            }
            console.log("User ID:", user._id);
            req.session.userId = user._id;
            console.log("Session after setting userId:", req.session)
            res.redirect('/users/profile');
        });
    },

    profile: function(req, res,next){
        UserModel.findById(req.session.userId)
        .exec(function(error, user){
            if(error){
                return next(error);
            } else{
                if(user===null){
                    var err = new Error('Not authorized, go back!');
                    err.status = 400;
                    return next(err);
                } else{
                    const canEdit = true;
                    return res.render('user/profile', {user, canEdit});
                }
            }
        });  
    },

    logout: function(req, res, next){
        if(req.session){
            req.session.destroy(function(err){
                if(err){
                    return next(err);
                } else{
                    return res.redirect('/');
                }
            });
        }
    },

    addPicture: function(req, res){
        upload(req, res, function(err) {
            if (err instanceof multer.MulterError) {
                return res.status(500).json({ message: 'Multer error occurred when uploading.', error: err });
            } else if (err) {
                return res.status(500).json({ message: 'Unknown error occurred when uploading.', error: err });
            }
    
            const filePath = '/images/' + req.file.originalname; 
            const userId = req.session.userId;
    
            // Update user document with the new profile picture URL
            UserModel.findByIdAndUpdate(userId, { profilePicture: filePath }, { new: true }, (err, user) => {
                if (err) {
                    return res.status(500).json({
                        message: 'Error updating user with new profile picture',
                        error: err
                    });
                }
                const canEdit = true;
                return res.render('user/profile', {user, canEdit});
            });
        });
    },
    showProfile: function(req,res){
        UserModel.findById(req.params.id)
        .exec(function(error, user){
            if(error){
                return next(error);
            } else{
                if(user===null){
                    var err = new Error('Not authorized, go back!');
                    err.status = 400;
                    return next(err);
                } else{
                    const canEdit = (req.params.id.toString() === req.session.userId.toString());
                    return res.render('user/profile', {user, canEdit});
                }
            }
        });  
    }
};
