var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

const viewSchema = new Schema({
    viewedAt: { type: Date, default: Date.now }
});

var questionSchema = new Schema({
	'questionText' : String,
	'description' : String,
	'created' : { type: Date, default: Date.now },
	'creator': {
		type: Schema.Types.ObjectId,
		ref: 'AnotherModel'
	},
	'anwsers': [{ type: Schema.Types.ObjectId, ref: 'anwsers' }],
	'views': [viewSchema],
});

module.exports = mongoose.model('question', questionSchema);
