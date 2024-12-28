#include <iostream>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>
#include <thread>
#include <atomic>
#include <mutex>
#include <condition_variable>
#include <chrono>
#include <openssl/evp.h> // Use EVP API for OpenSSL 3.0

// Mutex for synchronized console output
std::mutex output_mutex;

// Atomic flag to signal that a block has been mined
std::atomic<bool> block_mined(false);

// Condition variable and mutex to notify main thread when a nonce is found
std::mutex cv_mutex;
std::condition_variable cv;

// Atomic counter to accumulate total operations across all threads
std::atomic<int> total_ops(0);

// Function to convert hash bytes to a hexadecimal string
std::string hash_to_hex(const unsigned char* hash, size_t length) {
    std::ostringstream oss;
    for (size_t i = 0; i < length; ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return oss.str();
}

// Optimized mining function using EVP API
void mine_block_optimized(const std::string& prepacked_data, int difficulty, int thread_id, int num_threads, 
                          std::atomic<int>& result_nonce) {
    try {
        // Initialize EVP_MD_CTX
        EVP_MD_CTX* ctx = EVP_MD_CTX_new();
        if (!ctx) {
            throw std::runtime_error("Failed to create EVP_MD_CTX");
        }

        // Initialize digest context for SHA-256
        if (EVP_DigestInit_ex(ctx, EVP_sha256(), nullptr) != 1) {
            EVP_MD_CTX_free(ctx);
            throw std::runtime_error("Failed to initialize SHA-256 digest");
        }

        // Update context with prepacked data
        if (EVP_DigestUpdate(ctx, prepacked_data.data(), prepacked_data.size()) != 1) {
            EVP_MD_CTX_free(ctx);
            throw std::runtime_error("Failed to update SHA-256 digest with prepacked_data");
        }

        // Prepare buffer for nonce (prepacked_data + nonce)
        const size_t buffer_size = prepacked_data.size() + sizeof(uint32_t);
        std::vector<unsigned char> buffer(prepacked_data.begin(), prepacked_data.end());
        buffer.resize(buffer_size, 0);

        uint32_t* nonce_ptr = reinterpret_cast<uint32_t*>(&buffer[prepacked_data.size()]);
        *nonce_ptr = thread_id; // Initialize nonce with thread_id to ensure unique starting points

        int full_bytes = difficulty / 2;
        int remaining_nibble = difficulty % 2;

        // Clone the base EVP_MD_CTX for this thread
        // Note: EVP_MD_CTX_clone is not thread-safe, so we initialize and reuse the context
        // Each thread maintains its own context

        while (!block_mined.load(std::memory_order_relaxed)) {
            // Reset the digest context to the prepacked state
            if (EVP_DigestInit_ex(ctx, nullptr, nullptr) != 1) { // nullptr keeps the same digest type
                throw std::runtime_error("Failed to reset SHA-256 digest");
            }

            // Update context with prepacked data
            if (EVP_DigestUpdate(ctx, prepacked_data.data(), prepacked_data.size()) != 1) {
                throw std::runtime_error("Failed to update SHA-256 digest with prepacked_data");
            }

            // Update context with nonce
            if (EVP_DigestUpdate(ctx, nonce_ptr, sizeof(uint32_t)) != 1) {
                throw std::runtime_error("Failed to update SHA-256 digest with nonce");
            }

            // Finalize the digest
            unsigned char hash[EVP_MAX_MD_SIZE];
            unsigned int length = 0;
            if (EVP_DigestFinal_ex(ctx, hash, &length) != 1) {
                throw std::runtime_error("Failed to finalize SHA-256 digest");
            }

            // Increment the total operations counter
            total_ops++;

            // Check if the hash meets the difficulty requirements
            bool valid = true;
            for (int i = 0; i < full_bytes; ++i) {
                if (hash[i] != 0x00) {
                    valid = false;
                    break;
                }
            }

            if (valid && remaining_nibble) {
                if ((hash[full_bytes] >> 4) != 0x0) {
                    valid = false;
                }
            }

            // If a valid nonce is found, signal all threads to stop
            if (valid) {
                uint32_t found_nonce = *nonce_ptr;
                if (!block_mined.exchange(true)) { // Only the first thread to find a nonce sets it
                    result_nonce.store(found_nonce);
                    std::string hash_hex = hash_to_hex(hash, length);
                    {
                        std::lock_guard<std::mutex> lock(output_mutex);
                        std::cout << "Thread " << thread_id << " mined the block with nonce: " 
                                  << found_nonce << ", Hash: " << hash_hex << std::endl;
                    }
                    // Notify the condition variable
                    {
                        std::lock_guard<std::mutex> lock(cv_mutex);
                        cv.notify_one();
                    }
                }
                break; // Exit loop as block is mined
            }

            // Increment nonce by number of threads to ensure unique nonces across threads
            *nonce_ptr += num_threads;
        }

        // Clean up the digest context
        EVP_MD_CTX_free(ctx);
    } catch (const std::exception& e) {
        std::lock_guard<std::mutex> lock(output_mutex);
        std::cerr << "Error in thread " << thread_id << ": " << e.what() << std::endl;
    }
}

// Exposed function for Python
extern "C" const char* mine_blocks(const char* prepacked_data_c, int data_length, int difficulty, int num_threads) {
    static std::string result; // Persistent result to return to Python

    // Reset the mined flag and total operations counter
    block_mined.store(false);
    total_ops.store(0);

    std::atomic<int> result_nonce(-1);
    std::vector<std::thread> threads;

    // Construct prepacked_data with the correct length
    std::string prepacked_data(prepacked_data_c, data_length);

    // Log the prepacked data for verification
    std::ostringstream oss;
    for (size_t i = 0; i < prepacked_data.size(); ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (int)(unsigned char)prepacked_data[i];
    }
    {
        std::lock_guard<std::mutex> lock(output_mutex);
        std::cout << "Prepacked Data (C++): " << oss.str() << std::endl;
    }

    // Start the timer
    auto start_time = std::chrono::high_resolution_clock::now();

    // Launch threads
    for (int i = 0; i < num_threads; ++i) {
        threads.emplace_back(mine_block_optimized, std::ref(prepacked_data), difficulty, i, num_threads, 
                             std::ref(result_nonce));
    }

    // Wait for a thread to find a nonce
    {
        std::unique_lock<std::mutex> lock(cv_mutex);
        cv.wait(lock, []{ return block_mined.load(); });
    }

    // At this point, a nonce has been found and the flag is set
    // Wait for all threads to join (they should exit quickly if on P-cores)
    for (auto& t : threads) {
        if (t.joinable()) {
            t.join();
        }
    }

    // Stop the timer
    auto end_time = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> elapsed_time = end_time - start_time;

    // Calculate total operations and speed
    int operations = total_ops.load();
    double speed = operations / elapsed_time.count();

    // Prepare the result string
    result = "Nonce: " + std::to_string(result_nonce.load()) + 
             ", Time Taken: " + std::to_string(elapsed_time.count()) + " seconds, " +
             "Speed: " + std::to_string(speed) + " ops/sec";

    return result.c_str();
}

// Function to calculate block hash (unchanged, but updated to use EVP API)
extern "C" const char* calculate_block_hash(const char* prepacked_data_c, int data_length, uint32_t nonce) {
    static std::string result; // Persistent result for return
    std::string prepacked_data(prepacked_data_c, data_length);

    // Append the nonce to the prepacked data
    const size_t buffer_size = prepacked_data.size() + sizeof(uint32_t);
    std::vector<unsigned char> buffer(prepacked_data.begin(), prepacked_data.end());
    buffer.resize(buffer_size, 0);
    uint32_t* nonce_ptr = reinterpret_cast<uint32_t*>(&buffer[prepacked_data.size()]);
    *nonce_ptr = nonce;

    try {
        // Initialize EVP_MD_CTX
        EVP_MD_CTX* ctx = EVP_MD_CTX_new();
        if (!ctx) {
            throw std::runtime_error("Failed to create EVP_MD_CTX");
        }

        // Initialize digest context for SHA-256
        if (EVP_DigestInit_ex(ctx, EVP_sha256(), nullptr) != 1) {
            EVP_MD_CTX_free(ctx);
            throw std::runtime_error("Failed to initialize SHA-256 digest");
        }

        // Update context with buffer data
        if (EVP_DigestUpdate(ctx, buffer.data(), buffer.size()) != 1) {
            EVP_MD_CTX_free(ctx);
            throw std::runtime_error("Failed to update SHA-256 digest with buffer data");
        }

        // Finalize the digest
        unsigned char hash[EVP_MAX_MD_SIZE];
        unsigned int length = 0;
        if (EVP_DigestFinal_ex(ctx, hash, &length) != 1) {
            EVP_MD_CTX_free(ctx);
            throw std::runtime_error("Failed to finalize SHA-256 digest");
        }

        // Clean up the digest context
        EVP_MD_CTX_free(ctx);

        // Convert hash to hex string
        std::ostringstream oss;
        for (size_t i = 0; i < length; ++i) {
            oss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
        }
        result = oss.str();
    } catch (const std::exception& e) {
        std::lock_guard<std::mutex> lock(output_mutex);
        std::cerr << "Error in calculate_block_hash: " << e.what() << std::endl;
    }

    return result.c_str();
}


/*
g++ -o libmultithread.so -shared -fPIC -std=c++11 -pthread -O3 -I/opt/homebrew/opt/openssl@3/include -L/opt/homebrew/opt/openssl@3/lib -lssl -lcrypto multithread.cpp
*/