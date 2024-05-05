var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var anwsersSchema = new Schema({
	'anwserText' : String,
	'upvotes' : { type: Number, default: 0 },
	'downvotes' : { type: Number, default: 0 },
	'created' : { type: Date, default: Date.now },
	'creator' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'approved': { type: Boolean, default: false }
});

module.exports = mongoose.model('anwsers', anwsersSchema);
