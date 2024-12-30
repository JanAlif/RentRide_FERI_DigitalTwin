from mpi4py import MPI
import json
import logging
import random
import struct
import tkinter as tk
import traceback
import hashlib
import time
import socket
import threading
import queue
import ctypes
import os
import sys

# Initialize MPI
comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

# Define serialization functions
def serialize_genesis_block(genesis):
    """Serialize the genesis block into a JSON-formatted byte string."""
    return json.dumps({
        'index': genesis.index,
        'timestamp': genesis.timestamp,
        'data': genesis.data,
        'previous_hash': genesis.previous_hash,
        'difficulty': genesis.difficulty,
        'nonce': genesis.nonce,
        'hash': genesis.hash
    }).encode('utf-8')

def deserialize_genesis_block(serialized):
    """Deserialize the JSON-formatted byte string back into a Block object."""
    data = json.loads(serialized.decode('utf-8'))
    genesis = Block(
        index=data['index'],
        timestamp=data['timestamp'],
        data=data['data'],
        previous_hash=data['previous_hash'],
        difficulty=data['difficulty']
    )
    genesis.nonce = data['nonce']
    genesis.set_hash(data['hash'])
    return genesis

# Load the shared library
lib_path = os.path.join(os.path.dirname(__file__), 'libmultithread.so')  # Use 'multithread.dll' on Windows
try:
    lib = ctypes.CDLL(lib_path)
except OSError as e:
    print(f"Error loading shared library: {e}")
    sys.exit(1)

# Set the function signatures
lib.mine_blocks.argtypes = [ctypes.c_char_p, ctypes.c_int, ctypes.c_int, ctypes.c_int]
lib.mine_blocks.restype = ctypes.c_char_p

lib.calculate_block_hash.argtypes = [ctypes.c_char_p, ctypes.c_int, ctypes.c_uint32]
lib.calculate_block_hash.restype = ctypes.c_char_p

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format=f'%(asctime)s - %(levelname)s - [Process {rank + 1}/{size}] - %(message)s',
    handlers=[
        logging.FileHandler(f'process_{rank + 1}.log'),
        logging.StreamHandler(sys.stdout)
    ]
)

# Initialize threading lock for blockchain
blockchain_lock = threading.Lock()

# Initialize global variables
stop_signal = False  
server_socket = None  
client_sockets = []  
PEER_DATA = {}
blockchain = []

# Initialize counters
operations_count = 0
total_operations = 0
start_time = None

# Initialize a queue for GUI updates
gui_queue = queue.Queue()

# Initialize global variables for timing
mining_start_time = None
mining_elapsed_time_recorded = False

# Define the Block class and all functions as in your original script
# ... [Your existing Block class and all other functions remain unchanged] ...

class Block:
    def __init__(self, index, timestamp, data, previous_hash, difficulty):
        self.index = index
        self.timestamp = timestamp
        self.data = data
        self.previous_hash = previous_hash
        self.difficulty = difficulty
        self.nonce = 0
        self.prepacked_data = self.prepare_packed_data()
        self.hash = self.calculate_hash(self.nonce)

    def prepare_packed_data(self):
        """Prepack the constant parts of the block data."""
        # Encode data and previous_hash
        data_bytes = self.data.encode()
        previous_hash_bytes = self.previous_hash.encode()
        # Fixed struct format based on lengths of data and previous_hash
        struct_format = f">I d {len(data_bytes)}s {len(previous_hash_bytes)}s I"
        return struct.pack(
            struct_format,
            self.index,
            self.timestamp,
            data_bytes,
            previous_hash_bytes,
            self.difficulty
        )

    def calculate_hash(self, nonce):
        """Calculate the SHA-256 hash with the given nonce using C++."""
        prepacked_data = self.prepacked_data

        # Log debug information
        logging.debug(f"Prepacked Data: {prepacked_data.hex()}")
        logging.debug(f"Nonce: {nonce}")

        # Call C++ function with prepacked_data and its length
        block_hash = lib.calculate_block_hash(prepacked_data, len(prepacked_data), nonce)
        if isinstance(block_hash, bytes):
            block_hash = block_hash.decode('utf-8').strip()

        logging.info(f"Hash from C++: {block_hash}")
        return block_hash

    def set_hash(self, hash_value):
        """Set the hash value of the block."""
        self.hash = hash_value

def create_genesis_block():
    """Create the genesis (first) block of the blockchain using the C++ mining function."""
    fixed_timestamp = 1672531200.0  # Example: 2023-01-01 00:00:00 UTC
    fixed_difficulty = 5  # Adjust difficulty as needed

    genesis = Block(
        index=0,
        timestamp=fixed_timestamp,
        data="Genesis Block",
        previous_hash="0",
        difficulty=fixed_difficulty
    )

    # Log the prepacked data for verification
    prepacked_data = genesis.prepacked_data
    logging.info(f"Prepacked Data (Python): {prepacked_data.hex()}")

    num_threads = 4  # Number of threads for mining; can be configurable

    # Call the C++ mining function with prepacked_data and its length
    result_bytes = lib.mine_blocks(prepacked_data, len(prepacked_data), fixed_difficulty, num_threads)
    result_str = result_bytes.decode('utf-8')

    # Parse the result from C++
    try:
        parts = result_str.split(',')
        nonce_part = parts[0].strip()
        nonce = int(nonce_part.split(':')[1].strip())
        genesis.nonce = nonce
        genesis.hash = genesis.calculate_hash(nonce)  # Calculate the hash with the mined nonce
        logging.info(f"Genesis block mined with hash: {genesis.hash} and nonce: {nonce}")
    except Exception as e:
        logging.error(f"Error parsing genesis block mining result: {e}")
        raise

    return genesis

def adjust_difficulty(chain):
    """Adjust the mining difficulty based on the length of the chain."""
    if len(chain) % 5 == 0 and len(chain) != 0:  
        return chain[-1].difficulty + 1
    else:
        return chain[-1].difficulty

# Initialize the blockchain with the genesis block
if rank == 0:
    genesis_block = create_genesis_block()
    serialized_genesis = serialize_genesis_block(genesis_block)
else:
    serialized_genesis = None

# Broadcast the serialized genesis block to all processes
serialized_genesis = comm.bcast(serialized_genesis, root=0)

# Deserialize the genesis block for non-root processes
if rank != 0:
    genesis_block = deserialize_genesis_block(serialized_genesis)

# Initialize the blockchain with the genesis block
blockchain = [genesis_block]

# Finalize MPI to decouple processes
comm.Barrier()  # Ensure all processes have received the genesis block
MPI.Finalize()

# Continue with the rest of your program (GUI, P2P networking, etc.)

def start_node():
    """Start the node's server to accept peer connections."""
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

        logging.info(f"Node started on port {PORT}")

    except Exception as e:
        print("Offline: ")
        print(f"Error: {str(e)}")
        logging.error(f"Error starting node: {e}")
        status_display.set("Offline")

def operate_server():
    """Operate the server to accept incoming peer connections."""
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
        logging.error(f"Error during server operation: {e}")

    finally:
        for client_sock in client_sockets:
            client_sock.close()
        server_socket.close()
        print("Offline: Server stopped")
        logging.info("Server stopped")

def add_to_peer_data(message, color="black"):
    """Add messages to the peer data text box."""
    data_text1.config(state=tk.NORMAL)
    data_text1.insert(tk.END, message, color)
    data_text1.config(state=tk.DISABLED)

def handle_messages(message, color="black"):
    """Handle messages by adding them to the messages text box via the queue."""
    gui_queue.put((message, color))
    # Also print to console to retain original behavior
    print(message)

def mine_blocks():
    """Start the mining process in a separate thread."""
    global mining_start_time, mining_elapsed_time_recorded  # Declare as global
    try:
        # Start the timer
        mining_start_time = time.time()
        mining_elapsed_time_recorded = False  # Reset the flag

        mine_thread = threading.Thread(target=mine_blocks_thread, args=(node_name_entry.get(),), daemon=True)
        mine_thread.start()

        validation_thread = threading.Thread(target=validate_broadcast, daemon=True)
        validation_thread.start()

    except Exception as e:
        print(f"Error during mining: {str(e)}")
        logging.error(f"Error during mining: {e}")

def validate_broadcast():
    """Continuously validate the blockchain and broadcast if valid."""
    try:
        while True:
            if validate_chain():
                broadcast()
            time.sleep(5)

    except Exception as e:
        print(f"Error during blockchain validation and broadcasting: {str(e)}")
        logging.error(f"Error during blockchain validation and broadcasting: {e}")

def validate_chain(blockchain_to_validate=None):
    """Validate the blockchain."""
    try:
        if blockchain_to_validate is None:
            with blockchain_lock:
                chain = blockchain.copy()
        else:
            chain = blockchain_to_validate

        if len(chain) == 0:
            print("Validation failed: Blockchain is empty.")
            logging.warning("Validation failed: Blockchain is empty.")
            return False

        # Validate the genesis block
        genesis = chain[0]
        if genesis.previous_hash != "0":
            print("Validation failed: Genesis block has incorrect previous hash.")
            logging.warning("Validation failed: Genesis block has incorrect previous hash.")
            return False

        calculated_genesis_hash = genesis.calculate_hash(genesis.nonce)
        if genesis.hash != calculated_genesis_hash:
            print("Validation failed: Genesis block has incorrect hash.")
            print(f"Expected Hash: {calculated_genesis_hash}")
            logging.warning("Validation failed: Genesis block has incorrect hash.")
            return False

        # Iterate through the chain to validate each block
        for i in range(1, len(chain)):
            current_block = chain[i]
            previous_block = chain[i - 1]

            # Index Validation
            if current_block.index != previous_block.index + 1:
                print(f"Validation failed: Block {i} has an incorrect index.")
                logging.warning(f"Validation failed: Block {i} has an incorrect index.")
                return False

            # Previous Hash Validation
            if current_block.previous_hash != previous_block.hash:
                print(f"Validation failed: Block {i} has an incorrect previous hash.")
                logging.warning(f"Validation failed: Block {i} has an incorrect previous hash.")
                return False

            # Hash Validation
            calculated_hash = current_block.calculate_hash(current_block.nonce)
            if current_block.hash != calculated_hash:
                print(f"Validation failed: Block {i} has an incorrect hash.")
                print(f"Expected Hash: {calculated_hash}")
                logging.warning(f"Validation failed: Block {i} has an incorrect hash. Expected Hash: {calculated_hash}")
                return False

            # Difficulty Validation
            if not current_block.hash.startswith('0' * current_block.difficulty):
                print(f"Validation failed: Block {i} does not meet difficulty requirements.")
                logging.warning(f"Validation failed: Block {i} does not meet difficulty requirements.")
                return False

            # Timestamp Validation
            if i > 1:
                allowed_future = 240  # seconds
                if current_block.timestamp > previous_block.timestamp + allowed_future:
                    print(f"Validation failed: Block {i} has a timestamp too far in the future.")
                    logging.warning(f"Validation failed: Block {i} has a timestamp too far in the future.")
                    return False

                if current_block.timestamp < previous_block.timestamp + 1:
                    print(f"Validation failed: Block {i} has a timestamp earlier than the previous block.")
                    logging.warning(f"Validation failed: Block {i} has a timestamp earlier than the previous block.")
                    return False

        print("Blockchain validation successful")
        logging.info("Blockchain validation successful")
        return True

    except Exception as e:
        print(f"Error during blockchain validation: {str(e)}")
        logging.error(f"Error during blockchain validation: {e}")
        return False

def update_blocks_view(chain):
    """Update the blockchain view in the GUI."""
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
    """Broadcast the blockchain to all connected peers."""
    try:
        blockchain_str = serialize_chain()
        for client_socket in client_sockets:
            client_socket.sendall(blockchain_str.encode())

    except Exception as e:
        print(f"Error during blockchain broadcasting: {str(e)}")
        logging.error(f"Error during blockchain broadcasting: {e}")

def serialize_chain():
    """Serialize the blockchain into a string format."""
    try:
        return "##".join(
            f"{block.index}|{block.timestamp}|{block.data}|{block.previous_hash}|{block.difficulty}|{block.nonce}|{block.hash}"
            for block in blockchain
        )
    except Exception as e:
        print(f"Error during blockchain serialization: {str(e)}")
        logging.error(f"Error during blockchain serialization: {e}")
        return ""

def stop_node():
    """Stop the node and its server."""
    global stop_signal
    stop_signal = True
    if server_thread.is_alive():
        server_thread.join(timeout=2)
    print("Node stopped")
    logging.info("Node stopped")

def mine_blocks_thread(data):
    """The mining loop using C++ multithreaded mining."""
    global blockchain, mining_start_time, mining_elapsed_time_recorded  # Declare as global
    try:
        block_generation_interval = 10 
        diff_adjust_interval = 2
        # Use the optimal number of threads for this instance
        num_threads = calculate_threads_per_instance(logical_cores, size, rank)

        # Initialize counters
        operations_count = 0
        total_operations = 0
        start_time = time.time()
        last_report_time = start_time

        for _ in range(20):  # Mine 20 blocks; adjust as needed
            with blockchain_lock:
                previous_block = blockchain[-1]
                
                if len(blockchain) >= diff_adjust_interval:
                    prev_adjustment_block = blockchain[-diff_adjust_interval]
                else:
                    prev_adjustment_block = blockchain[0]

                time_expected = block_generation_interval * diff_adjust_interval
                time_taken = time.time() - prev_adjustment_block.timestamp

                if time_taken < (time_expected / 2):
                    difficulty = prev_adjustment_block.difficulty + 1
                elif time_taken > (time_expected * 2):
                    difficulty = max(prev_adjustment_block.difficulty - 1, 1)
                else:
                    difficulty = prev_adjustment_block.difficulty

                current_index = len(blockchain)
                timestamp = max(time.time(), previous_block.timestamp + 1 )

            # Prepare block template
            block_template = Block(
                index=current_index,
                timestamp=timestamp,
                data=data,
                previous_hash=previous_block.hash,
                difficulty=difficulty
            )
            prepacked_data = block_template.prepacked_data  # bytes
            
            # Call the C++ mining function with prepacked_data and its length
            result_bytes = lib.mine_blocks(prepacked_data, len(prepacked_data), difficulty, num_threads)
            result_str = result_bytes.decode('utf-8')

            # Parse the result
            # Expected format: "Nonce: {nonce}, Time Taken: {time} seconds, Speed: {speed} ops/sec"
            try:
                parts = result_str.split(',')
                nonce_part = parts[0].strip()
                nonce = int(nonce_part.split(':')[1].strip())
                # Optionally, parse time and speed if needed
            except Exception as e:
                print(f"Error parsing mining result: {e}")
                logging.error(f"Error parsing mining result: {e}")
                handle_messages("Error parsing mining result.", "red")
                continue

            # Calculate the hash using the nonce
            block_hash = block_template.calculate_hash(nonce)

            # Update the block with nonce and hash
            block_template.nonce = nonce
            block_template.hash = block_hash

            # Update the blockchain
            with blockchain_lock:
                # Verify that the previous_hash still matches
                if block_template.previous_hash != blockchain[-1].hash:
                    print("Chain updated during mining. Restarting mining.")
                    logging.warning("Chain updated during mining. Restarting mining.")
                    handle_messages("Chain updated during mining. Restarting mining.", "red")
                    continue  # Skip appending this block and restart mining

                blockchain.append(block_template)
                update_blocks_view(chain=blockchain)
                logging.info(f"Block {block_template.index} appended to the blockchain.")
                handle_messages(f"Block {block_template.index} mined with hash: {block_hash} difficulty: {difficulty}", "green")

                # Check if we have reached 10 blocks
                if len(blockchain) == 10 and not mining_elapsed_time_recorded:
                    mining_end_time = time.time()
                    elapsed_time = mining_end_time - mining_start_time
                    mining_elapsed_time_recorded = True  # Ensure this runs only once
                    message = f"Time taken to mine 10 blocks: {elapsed_time:.2f} seconds"
                    logging.info(message)
                    gui_queue.put((message, "blue"))  # Display in GUI


            # Update operations count and speed
            # Extract speed from result_str
            try:
                speed_part = parts[2].strip()
                speed = float(speed_part.split(':')[1].strip().split()[0])
                root.after(0, ops_label.config, {"text": f"Ops/sec: {speed:.2f}"})
                print(f"Ops/sec: {speed:.2f}")
            except Exception as e:
                print(f"Error parsing speed: {e}")
                logging.error(f"Error parsing speed: {e}")

        # Optionally, calculate and display average operations per second
        end_time = time.time()
        total_time = end_time - start_time
        if total_time > 0:
            average_ops_per_sec = total_operations / total_time
            root.after(0, ops_label.config, {"text": f"Average Ops/sec: {average_ops_per_sec:.2f}"})
            print(f"Average operations per second: {average_ops_per_sec:.2f}")
            logging.info(f"Average operations per second: {average_ops_per_sec:.2f}")
        else:
            root.after(0, ops_label.config, {"text": "Ops/sec: 0"})
            print("No operations counted.")
            logging.warning("No operations counted.")

    except Exception as e:
        print(f"Error during mining blocks: {str(e)}")
        logging.error(f"Error during mining blocks: {e}")
    finally:
        pass  # Any final cleanup can be done here

def on_shutdown():
    """Handle the shutdown of the application."""
    try:
        stop_node()
    except Exception as e:
        print(f"Error stopping server: {str(e)}")
        logging.error(f"Error stopping server: {e}")
    root.destroy()

def connect_to_host():
    """Connect to a peer node."""
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
        print(f"Connected to host {HOST}:{PORT}")
        logging.info(f"Connected to host {HOST}:{PORT}")

    except Exception as e:
        print(f"Error connecting to host: {e}")
        logging.error(f"Error connecting to host: {e}")
        status_display.set("Offline")
        status_label.place(x=330, y=160)

def receive_data(client_sock):
    """Receive data from a connected peer."""
    try:
        while True:
            data = client_sock.recv(8192).decode()
            if not data:
                break
            update_chain(data)
    except Exception as e:
        print(f"Error in receiving messages: {e}")
        logging.error(f"Error in receiving messages: {e}")

def update_chain(received_data):
    """Update the local blockchain with received data."""
    global blockchain
    try:
        received_blockchain = deserialize_chain(received_data)

        if validate_chain(blockchain_to_validate=received_blockchain):
            if cumulative_difficulty(received_blockchain) > cumulative_difficulty(blockchain):
                # Ensure the received chain's last block has a timestamp >= local last block's timestamp
                if received_blockchain[-1].timestamp >= blockchain[-1].timestamp:
                    with blockchain_lock:
                        blockchain = received_blockchain
                        print("Local Blockchain Updated\n")
                        update_blocks_view(chain=received_blockchain)
                        logging.info("Local blockchain updated from received chain.")
                        gui_queue.put(("Local Blockchain Updated", "green"))
                else:
                    print("Received Blockchain has an earlier timestamp. Ignored.\n")
                    logging.warning("Received blockchain has an earlier timestamp. Ignored.")
                    gui_queue.put(("Received Blockchain has an earlier timestamp. Ignored.", "red"))
            else:
                print("Received Blockchain Validated but Not Longer\n")
                logging.info("Received blockchain is validated but not longer than local chain.")
                gui_queue.put(("Received Blockchain Validated but Not Longer", "blue"))
        else:
            add_to_peer_data("Received Blockchain Validation Failed\n")
            logging.warning("Received blockchain validation failed.")

    except Exception as e:
        print(f"Error updating blockchain: {e}")
        logging.error(f"Error updating blockchain: {e}")

def cumulative_difficulty(blockchain):
    """Calculate the cumulative difficulty of the blockchain."""
    return sum(2 ** block.difficulty for block in blockchain)

def deserialize_chain(data):
    """Deserialize the blockchain string into Block objects."""
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
                    difficulty=difficulty
                )
                block.nonce = nonce
                block.set_hash(hash_value)

                received_blockchain.append(block)
            else:
                print(f"Error: Insufficient data in block string: {block_data}")
                logging.error(f"Error: Insufficient data in block string: {block_data}")
        return received_blockchain

    except Exception as e:
        traceback.print_exc()
        print(f"Error during deserialization: {e}")
        logging.error(f"Error during deserialization: {e}")
        return []

HOST = "127.0.0.1"
PORT = None

# Initialize the Tkinter GUI
def initialize_gui():
    global root, data_text2, node_name_label, node_name_entry, connect_button, mine_button
    global ip_label, host_name_entry, connect_host_button, status_display, status_label
    global data_text1, ops_label

    root = tk.Tk()
    root.title(f"Blockchain - Process {rank + 1}/{size}")
    root.geometry("700x760")

    # Define text tags for coloring
    data_text2 = tk.Text(root, wrap=tk.WORD, height=20, width=70, state=tk.DISABLED)
    data_text2.place(x=100, y=420)
    data_text2.tag_configure("black", foreground="black")
    data_text2.tag_configure("green", foreground="green")
    data_text2.tag_configure("blue", foreground="blue")
    data_text2.tag_configure("red", foreground="red")

    # Node Key Entry
    node_name_label = tk.Label(root, text="Key:")
    node_name_label.place(x=220, y=10)

    node_name_entry = tk.Entry(root)
    node_name_entry.place(x=260, y=10)

    # Start Node Button
    connect_button = tk.Button(root, text="Start", command=start_node)
    connect_button.place(x=280, y=40)

    # Mine Button
    mine_button = tk.Button(root, text="Mine", command=mine_blocks)
    mine_button.place(x=360, y=40)

    # Host IP Entry
    ip_label = tk.Label(root, text="IP:")
    ip_label.place(x=230, y=80)

    host_name_entry = tk.Entry(root)
    host_name_entry.place(x=260, y=80)

    # Connect to Host Button
    connect_host_button = tk.Button(root, text="Connect", command=connect_to_host)
    connect_host_button.place(x=305, y=110)

    # Status Display
    status_display = tk.StringVar()
    status_display.set("Offline")
    status_label = tk.Label(root, textvariable=status_display)
    status_label.place(x=330, y=160)

    # Blockchain View Text Box
    data_text1 = tk.Text(root, wrap=tk.WORD, height=15, width=45, state=tk.DISABLED)
    data_text1.place(x=185, y=200)

    # Operations Per Second Label
    ops_label = tk.Label(root, text="Ops/sec: 0")
    ops_label.place(x=300, y=180)

    # Threads Assigned Label
    threads_label = tk.Label(root, text=f"Threads Assigned: {num_threads}")
    threads_label.place(x=300, y=700)

    # Start processing the GUI queue
    root.after(100, process_gui_queue)

    # Handle shutdown
    root.protocol("WM_DELETE_WINDOW", on_shutdown)
    root.mainloop()

def calculate_threads_per_instance(total_cores, num_instances, rank):
    """Calculate the number of threads for each instance based on rank."""
    base_threads = total_cores // num_instances
    extra_threads = total_cores % num_instances

    # Assign one extra thread to the first 'extra_threads' instances
    if rank < extra_threads:
        return base_threads + 1
    else:
        return base_threads

# Calculate threads for this instance
logical_cores = 11  # Total logical cores
num_threads = calculate_threads_per_instance(logical_cores, size, rank)

def main():
    # Initialize the GUI
    initialize_gui()

def process_gui_queue():
    """Process messages from the GUI queue."""
    try:
        while not gui_queue.empty():
            message, color = gui_queue.get_nowait()
            data_text2.config(state=tk.NORMAL)
            data_text2.insert(tk.END, message + "\n", color)
            data_text2.tag_add(color, f"{data_text2.index(tk.END)}-{len(message)}c", tk.END)
            data_text2.config(state=tk.DISABLED)
        root.after(100, process_gui_queue)
    except Exception as e:
        print(f"Error processing GUI queue: {e}")
        logging.error(f"Error processing GUI queue: {e}")

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        logging.error(f"Unhandled exception: {e}\n{traceback.format_exc()}")
        sys.exit(1)


#mpiexec -n 2 python vaja5.py