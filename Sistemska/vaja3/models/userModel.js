var mongoose = require('mongoose');
var Schema   = mongoose.Schema;
var bcrypt = require('bcrypt');

var userSchema = new Schema({
	'username' : String,
	'email' : String,
	'password' : String
});


userSchema.pre('save', function(next) {
    if (!this.isModified('password')) return next();

    bcrypt.hash(this.password, 10, (err, hash) => {
        if (err) return next(err);
        this.password = hash;
        next();
    });
});


userSchema.statics.authenticate = function(username, password, callback) {
    this.findOne({ username: username })
        .exec(function(err, user) {
            if (err) {
                return callback(err);
            } else if (!user) {
                var err = new Error("user not found");
                err.status = 401;
                return callback(); // User not found
            }
            
            bcrypt.compare(password, user.password, function(err, result) {
                if (result === true) {
                    return callback(null, user);
                } else {
                    return callback(); // Incorrect password
                }
            });
        });
};

userSchema.methods.updatePassword = function(newPassword, callback) {
    this.password = newPassword; // Set the new password
    this.save(callback); // Save the user, `pre('save')` will hash the password
};

module.exports = mongoose.model('user', userSchema);