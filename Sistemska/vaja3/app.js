var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var session = require('express-session');
var MongoStore = require('connect-mongo');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/userRoutes');
var photoRouter = require('./routes/photosRoutes');
var questionRouter = require('./routes/QuestionRoutes');
var answerRouter = require('./routes/AnswerRoutes');
var commentRouter = require('./routes/CommentRoutes');

var mongoose = require('mongoose');
var mongoDB = 'mongodb://127.0.0.1/vaja3exp';
mongoose.connect(mongoDB);
mongoose.Promise = global.Promise;
var db = mongoose.connection;

db.on('error', function(err) {
  console.error('Connection error:', err);
});

// Database open success message
db.once('open', function() {
  console.log('Database connected successfully.');
});

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use(session({
  secret: 'work hard',
  resave: true,
  saveUninitialized: false,
  store: MongoStore.create({mongoUrl: mongoDB})
}));

app.use(express.static('public'));

app.use('/', indexRouter);
app.use('/user', usersRouter);
app.use('/photo', photoRouter);
app.use('/question', questionRouter);
app.use('/answer', answerRouter);
app.use('/comment', commentRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

app.use('/public', express.static('public'));

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
