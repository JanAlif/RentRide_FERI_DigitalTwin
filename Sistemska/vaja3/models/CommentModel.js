var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var CommentSchema = new Schema({
	'questionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'Question'
	},
	'answerId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'Answer'
	},
	'content' : String,
	'postedBy' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'postedDate' : Date
});

module.exports = mongoose.model('Comment', CommentSchema);
