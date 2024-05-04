var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var photosSchema = new Schema({
	'name' : String,
	'path' : String,
	'usedBy' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'User'
	}
});

module.exports = mongoose.model('photos', photosSchema);
