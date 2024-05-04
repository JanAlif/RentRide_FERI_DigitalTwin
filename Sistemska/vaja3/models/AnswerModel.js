var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var AnswerSchema = new Schema({
	'questionId': {
		type: Schema.Types.ObjectId,
		ref: 'Question'
	},
	'content': String,
	'postedBy': {
		type: Schema.Types.ObjectId,
		ref: 'user'
	},
	'postedDate': Date,
	upvotes: [{ type: Schema.Types.ObjectId, ref: 'user' }],
	downvotes: [{ type: Schema.Types.ObjectId, ref: 'user' }],
	'isAccepted': Boolean
});

module.exports = mongoose.model('Answer', AnswerSchema);
