import json
import random
import tkinter as tk
import traceback
import hashlib
import time
import socket
import threading

stop_signal = False  
server_socket = None  
client_sockets = []  
PEER_DATA = {}
blockchain = []

class Block:
    def __init__(self, index, timestamp, data, previous_hash, difficulty, nonce):
        self.index = index
        self.timestamp = timestamp
        self.data = data
        self.difficulty = difficulty
        self.nonce = nonce
        self.previous_hash = previous_hash
        self.hash = self.calculate_hash()

    def calculate_hash(self):
        block_header = {
            "index": self.index,
            "timestamp": self.timestamp,
            "data": self.data,
            "previous_hash": self.previous_hash,
            "difficulty": self.difficulty,
            "nonce": self.nonce
        }
        block_string = json.dumps(block_header, sort_keys=True)
        return hashlib.sha256(block_string.encode()).hexdigest()

    def set_hash(self, hash_value):
        self.hash = hash_value

def create_genesis_block():
    # Fixed timestamp for genesis block (e.g., 2023-01-01 00:00:00 UTC)
    fixed_timestamp = 1672531200.0  
    fixed_difficulty = 5
    fixed_nonce = 0

    # Create a genesis block that satisfies the difficulty requirement
    genesis = Block(
        index=0,
        timestamp=fixed_timestamp,
        data="Genesis Block",
        previous_hash="0",
        difficulty=fixed_difficulty,
        nonce=fixed_nonce
    )

    # Ensure genesis block's hash meets difficulty requirements
    while not genesis.hash.startswith('0' * fixed_difficulty):
        genesis.nonce += 1
        genesis.hash = genesis.calculate_hash()

    return genesis


def adjust_difficulty(chain):

    if len(chain) % 5 == 0:  
        return chain[-1].difficulty + 1
    else:
        return chain[-1].difficulty

genesis_block = create_genesis_block()
blockchain = [genesis_block]

def start_node():
    global server_thread, PORT, server_socket
    try:
        PORT = random.randint(1024, 65535)  
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(("127.0.0.1", PORT))
        server_socket.listen()

        server_thread = threading.Thread(target=operate_server, daemon=True)
        server_thread.start()
        status_display.set(f"Online: Port {PORT}")
        status_label.place(x=305, y=160)

    except Exception as e:
        print("Offline: ")
        print(f"Error: {str(e)}")

def operate_server():
    global PORT, stop_signal, server_socket, client_sockets
    try:
        while not stop_signal:
            try:
                client_sock, client_addr = server_socket.accept()
                print(f"Connected peer from {client_addr}")
                add_to_peer_data(f"Connected peer from {client_addr}\n")
                client_sockets.append(client_sock)
                threading.Thread(target=receive_data, args=(client_sock,), daemon=True).start()

            except socket.error:
                continue

    except Exception as e:
        print(f"Error during server operation: {str(e)}")

    finally:
        for client_sock in client_sockets:
            client_sock.close()
        server_socket.close()
        print("Offline: Server stopped")

def add_to_peer_data(message, color="black"):
    data_text1.config(state=tk.NORMAL)
    data_text1.insert(tk.END, message, color)
    data_text1.config(state=tk.DISABLED)

def handle_messages(message, color="black"):
    data_text2.config(state=tk.NORMAL)
    data_text2.insert(tk.END, message, color)
    data_text2.insert(tk.END, "\n")  
    data_text2.tag_add(color, f"{data_text2.index(tk.END)}-{len(message)}c", tk.END)
    data_text2.config(state=tk.DISABLED)

def mine_blocks():
    global blockchain
    try:
        # Remove the re-initialization of the genesis block
        # genesis_block = Block(index=0, timestamp=time.time(), data="Genesis Block", previous_hash="0", difficulty=1, nonce=0)
        # blockchain = [genesis_block]
        # update_blocks_view(chain=blockchain)
        
        mine_thread = threading.Thread(target=mine_blocks_thread, args=(node_name_entry.get(),), daemon=True)
        mine_thread.start()

        validation_thread = threading.Thread(target=validate_broadcast, daemon=True)
        validation_thread.start()

    except Exception as e:
        print(f"Error during mining: {str(e)}")

def validate_broadcast():
    try:
        while True:
            if validate_chain():
                broadcast()
            time.sleep(5)

    except Exception as e:
        print(f"Error during blockchain validation and broadcasting: {str(e)}")

def validate_chain(blockchain=None):
    try:
        if blockchain is None:
            blockchain = []  
        chain = blockchain
        for i in range(1, len(chain)):
            current_block = chain[i]
            previous_block = chain[i - 1]

            if current_block.index != previous_block.index + 1:
                print(f"Validation failed: Block {i} has an incorrect index.")
                return False

            if current_block.previous_hash != previous_block.hash:
                print(f"Validation failed: Block {i} has an incorrect previous hash.")
                return False

            calculated_hash = current_block.calculate_hash()
            if current_block.hash != calculated_hash:
                print(f"Validation failed: Block {i} has an incorrect hash.")
                print(f"Expected Hash: {calculated_hash}")
                return False

            # Timestamp Validation
            if i == 1:
                # **Skip timestamp validation for the first block after genesis**
                continue
            else:
                allowed_future = 120  # seconds
                if current_block.timestamp > previous_block.timestamp + allowed_future:
                    print(f"Validation failed: Block {i} has a timestamp too far in the future.")
                    return False

                if current_block.timestamp < previous_block.timestamp:
                    print(f"Validation failed: Block {i} has a timestamp earlier than the previous block.")
                    return False


        print("Blockchain validation successful")
        return True

    except Exception as e:
        print(f"Error during blockchain validation: {str(e)}")
        return False

def update_blocks_view(chain):
    data_text1.config(state=tk.NORMAL)
    data_text1.delete("1.0", tk.END)
    data_text1.config(state=tk.DISABLED)

    for block in chain:
        add_to_peer_data(f"Index {block.index}:\n")
        add_to_peer_data(f"Timestamp: {block.timestamp}\n")
        add_to_peer_data(f"Data: {block.data}\n")
        add_to_peer_data(f"Previous Hash: {block.previous_hash}\n")
        add_to_peer_data(f"Difficulty: {block.difficulty}\n")
        add_to_peer_data(f"Nonce: {block.nonce}\n")
        add_to_peer_data(f"Hash: {block.hash}\n")
        add_to_peer_data("\n")

def broadcast():
    try:
        for client_socket in client_sockets:
            blockchain_str = serialize_chain()
            client_socket.sendall(blockchain_str.encode())

    except Exception as e:
        print(f"Error during blockchain broadcasting: {str(e)}")

def serialize_chain():
    try:
        blockchain_str = ""
        for block in blockchain:
            blockchain_str += f"{block.index}|{block.timestamp}|{block.data}|{block.previous_hash}|{block.difficulty}|{block.nonce}|{block.hash}##"
        return blockchain_str
    except Exception as e:
        print(f"Error during blockchain serialization: {str(e)}")
        return ""
    pass

def stop_node():
    global stop_signal
    stop_signal = True
    server_thread.join(timeout=2)

def mine_blocks_thread(data):
    try:
        block_generation_interval = 10 
        diff_adjust_interval = 2

        for _ in range(10):  

            previous_block = blockchain[-1]
            timestamp = max(time.time(), previous_block.timestamp + 1)  
            
            if len(blockchain) >= diff_adjust_interval:
                prev_adjustment_block = blockchain[-diff_adjust_interval]
            else:

                prev_adjustment_block = blockchain[0]

            time_expected = block_generation_interval * diff_adjust_interval
            time_taken = timestamp - prev_adjustment_block.timestamp

            if time_taken < (time_expected / 2):
                difficulty = prev_adjustment_block.difficulty + 1
            elif time_taken > (time_expected * 2):
                difficulty = prev_adjustment_block.difficulty - 1
            else:
                difficulty = prev_adjustment_block.difficulty

            nonce = 0

            while True:

                current_index = len(blockchain)  

                block = Block(current_index, timestamp, data, previous_block.hash, difficulty, nonce)
                hash_value = block.hash

                if hash_value.startswith('0' * difficulty):
                    print(f"Block mined with hash: {hash_value}")
                    handle_messages(f"{hash_value} difficulty: {block.difficulty}", "green")
                    root.update()
                    time.sleep(0.1)  
                    break
                else:
                    nonce += 1  

            blockchain.append(block)
            update_blocks_view(chain=blockchain)

            current_index += 1  

            root.update()
            time.sleep(0.5)  

    except Exception as e:
        print(f"Error during mining blocks: {str(e)}")

def on_shutdown():
    try:
        global stop_signal
        stop_signal = True
        server_thread.join(timeout=2)
    except Exception as e:
        print(f"Error stopping server: {str(e)}")
    root.destroy()

def connect_to_host():
    global PORT
    try:
        host = host_name_entry.get()
        PORT = int(host)
        HOST = "127.0.0.1"
        client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_sock.connect((HOST, PORT))
        peer_name = f"Peer-{PORT}"
        PEER_DATA[peer_name] = {
            'address': (HOST, PORT)
        }
        client_sockets.append(client_sock)

    except Exception as e:
        status_display.set("Offline")
        status_label.place(x=330, y=160)

def receive_data(client_sock):
    try:
        while True:

            data = client_sock.recv(8192).decode()

            if not data:
                break

            update_chain(data)

    except Exception as e:
        print(f"Error in receiving messages: {str(e)}")

def update_chain(received_data):
    global blockchain
    try:

        received_blockchain = deserialize_chain(received_data)

        if validate_chain(blockchain=received_blockchain):

            if cumulative_difficulty(received_blockchain) > cumulative_difficulty(blockchain):
                blockchain = received_blockchain
                print("Local Blockchain Updated\n")
                update_blocks_view(chain=received_blockchain)
            else:
                print("Received Blockchain Validated but Not Longer\n")
        else:
            add_to_peer_data("Received Blockchain Validation Failed\n")

    except Exception as e:
        print(f"Error updating blockchain: {str(e)}")

def cumulative_difficulty(blockchain):
    cumulative_difficulty = 0
    for block in blockchain:
        cumulative_difficulty += 2 ** block.difficulty

    return cumulative_difficulty  

def deserialize_chain(data):
    try:

        block_strings = data.strip().split('##')

        received_blockchain = []
        for block_string in block_strings:

            if not block_string:
                continue

            block_data = block_string.split('|')

            if len(block_data) == 7:

                index = int(block_data[0])
                timestamp = float(block_data[1])
                data_value = block_data[2]
                previous_hash = block_data[3]
                difficulty = int(block_data[4])
                nonce = int(block_data[5])
                hash_value = block_data[6]

                block = Block(
                    index=index,
                    timestamp=timestamp,
                    data=data_value,
                    previous_hash=previous_hash,
                    difficulty=difficulty,
                    nonce=nonce
                )

                block.set_hash(hash_value)

                received_blockchain.append(block)
            else:
                print(f"Error: Insufficient data in block string: {block_data}")

        return received_blockchain

    except Exception as e:
        traceback.print_exc()  
        return []

HOST = "127.0.0.1"
PORT = None

root = tk.Tk()
root.title("Blockchain")

root.geometry("700x760")

node_name_label = tk.Label(root, text="Key:")
node_name_label.place(x=220, y=10)

node_name_entry = tk.Entry(root)
node_name_entry.place(x=260, y=10)

connect_button = tk.Button(root, text="Start", command=start_node)
connect_button.place(x=280, y=40)

mine_button = tk.Button(root, text="Mine", command=mine_blocks)
mine_button.place(x=360, y=40)

node_name_label = tk.Label(root, text="IP:")
node_name_label.place(x=230, y=80)
host_name_entry = tk.Entry(root)
host_name_entry.place(x=260, y=80)

connect_host_button = tk.Button(root, text="Connect", command=connect_to_host)
connect_host_button.place(x=305, y=110)

tk.Label(root, text="").place(y=50)

status_display = tk.StringVar()
status_display.set("Offline")
status_label = tk.Label(root, textvariable=status_display)
status_label.place(x=330, y=160)

data_text1 = tk.Text(root, wrap=tk.WORD, height=15, width=45, state=tk.DISABLED)
data_text1.place(x=185, y=200)

data_text2 = tk.Text(root, wrap=tk.WORD, height=15, width=70, state=tk.DISABLED)
data_text2.place(x=100, y=420)

root.protocol("WM_DELETE_WINDOW", on_shutdown)
root.mainloop()