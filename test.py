import socketio
import time

# URL of the Socket.IO server
SERVER_URL = 'http://localhost:4000'

# Admin token (make sure this matches the token on your server)
ADMIN_TOKEN = 'vrumvrum'

# Room name to create/join
ROOM_NAME = 'testRoom'

# Create a Socket.IO client
sio = socketio.Client()

# Connect to the server
@sio.event
def connect():
    print('Connected to server')

    # Create/join the specified room
    sio.emit('createRoom', ROOM_NAME)

    # Start emitting numbers every second
    emit_numbers()

# Handle room creation confirmation
@sio.on('roomCreated')
def on_room_created(room):
    print(f'Room created: {room}')

@sio.on('message')
def on_message(data):
    print(f'Received message: {data}')

# Emit a number every second
def emit_numbers():
    number = 0
    while True:
        sio.emit('emitToRoom', {
            'room': ROOM_NAME,
            'message': str(number)
        })
        print(f'Emitted number: {number}')
        number += 1
        time.sleep(1)

# Handle disconnection
@sio.event
def disconnect():
    print('Disconnected from server')

# Connect to the server with the admin token as a query parameter
sio.connect(SERVER_URL, headers={'token': ADMIN_TOKEN})

# Wait for events
sio.wait()