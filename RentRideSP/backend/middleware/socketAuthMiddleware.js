const socketAuthMiddleware = (socket, next) => {
    const token = socket.handshake.headers.token;
    console.log('Token:', token);
    if (token === process.env.JWT_CAR_SECRET) {
        socket.user = { role: 'admin' };
        next();
    }
    else {
        socket.user = { role: 'user' };
        next();
    }
}

export default socketAuthMiddleware