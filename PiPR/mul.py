import ctypes
import hashlib

# Load the shared library
lib = ctypes.CDLL('./libmultithread.so')  # Use 'multithread.dll' on Windows

# Set the function signature for the modified C++ function
lib.mine_blocks.argtypes = [ctypes.c_char_p, ctypes.c_int, ctypes.c_int, ctypes.c_int]
lib.mine_blocks.restype = ctypes.c_char_p

# Set the function signature for calculate_block_hash
lib.calculate_block_hash.argtypes = [ctypes.c_char_p, ctypes.c_int, ctypes.c_uint32]
lib.calculate_block_hash.restype = ctypes.c_char_p

# Define block parameters
prepacked_data = "Some Sample prepacked block data  "
difficulty = 7  # Adjust difficulty as needed
num_threads = 1  # Number of threads

# Prepare prepacked data as bytes and its length
prepacked_data_bytes = prepacked_data.encode('utf-8')
data_length = len(prepacked_data_bytes)

# Call the C++ mining function with the length of prepacked_data
result = lib.mine_blocks(prepacked_data_bytes, data_length, difficulty, num_threads)
print("Mining Result:", result.decode('utf-8'))

# Extract nonce from C++ result (Example parsing, adjust if format changes)
result_str = result.decode('utf-8')
parts = result_str.split(',')
nonce_part = parts[0].strip()
nonce = int(nonce_part.split(':')[1].strip())

# Mimic C++ SHA-256 hash calculation in Python
combined_data = prepacked_data_bytes + nonce.to_bytes(4, byteorder='little')
recalculated_hash = hashlib.sha256(combined_data).hexdigest()
print("Recalculated Hash:", recalculated_hash)