var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var QuestionSchema = new Schema({
	'title': String,
	'description': String,
	'postedBy': {
		type: Schema.Types.ObjectId,
		ref: 'user'
	},
	'postedDate': Date,
	'viewsCount': Number,
	'answers': [{
		type: Schema.Types.ObjectId,
		ref: 'Answer' 
	}],
	'acceptedAnswer': {
		type: Schema.Types.ObjectId,
		ref: 'Answer'
	}
});

module.exports = mongoose.model('Question', QuestionSchema);
