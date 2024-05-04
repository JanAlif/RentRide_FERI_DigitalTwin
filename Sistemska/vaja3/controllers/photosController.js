var PhotosModel = require('../models/photosModel.js');

/**
 * photosController.js
 *
 * @description :: Server-side logic for managing photoss.
 */
module.exports = {

    /**
     * photosController.list()
     */
    list: function (req, res) {
        PhotosModel.find(function (err, photoss) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting photos.',
                    error: err
                });
            }

            return res.json(photoss);
        });
    },

    /**
     * photosController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        PhotosModel.findOne({_id: id}, function (err, photos) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting photos.',
                    error: err
                });
            }

            if (!photos) {
                return res.status(404).json({
                    message: 'No such photos'
                });
            }

            return res.json(photos);
        });
    },

    /**
     * photosController.create()
     */
    create: function (req, res) {
        var photos = new PhotosModel({
			name : req.body.name,
			path : req.body.path,
			usedBy : req.body.usedBy
        });

        photos.save(function (err, photos) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating photos',
                    error: err
                });
            }

            return res.status(201).json(photos);
        });
    },

    /**
     * photosController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        PhotosModel.findOne({_id: id}, function (err, photos) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting photos',
                    error: err
                });
            }

            if (!photos) {
                return res.status(404).json({
                    message: 'No such photos'
                });
            }

            photos.name = req.body.name ? req.body.name : photos.name;
			photos.path = req.body.path ? req.body.path : photos.path;
			photos.usedBy = req.body.usedBy ? req.body.usedBy : photos.usedBy;
			
            photos.save(function (err, photos) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating photos.',
                        error: err
                    });
                }

                return res.json(photos);
            });
        });
    },

    /**
     * photosController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        PhotosModel.findByIdAndRemove(id, function (err, photos) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the photos.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
