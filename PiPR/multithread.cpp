#include <iostream>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>
#include <thread>
#include <atomic>
#include <mutex>
#include <chrono>
#include <openssl/evp.h> // EVP API for OpenSSL 3.0

std::mutex output_mutex; // For synchronized output
std::atomic<bool> block_mined(false); // Shared flag to stop all threads

// Function to convert hash bytes to hexadecimal string (only when a valid nonce is found)
std::string hash_to_hex(const unsigned char* hash, size_t length) {
    std::ostringstream oss;
    for (size_t i = 0; i < length; ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return oss.str();
}

void mine_block(const std::string& prepacked_data, int difficulty, int thread_id, int num_threads, 
                std::atomic<int>& result_nonce, int& thread_ops) {
    // Initialize base EVP_MD_CTX with prepacked_data
    EVP_MD_CTX* base_ctx = EVP_MD_CTX_new();
    if (!base_ctx) {
        throw std::runtime_error("Failed to create EVP_MD_CTX");
    }

    if (EVP_DigestInit_ex(base_ctx, EVP_sha256(), nullptr) != 1) {
        EVP_MD_CTX_free(base_ctx);
        throw std::runtime_error("Failed to initialize digest");
    }

    if (EVP_DigestUpdate(base_ctx, prepacked_data.data(), prepacked_data.size()) != 1) {
        EVP_MD_CTX_free(base_ctx);
        throw std::runtime_error("Failed to update digest with prepacked_data");
    }

    // Prepare buffer for nonce
    const size_t buffer_size = prepacked_data.size() + sizeof(uint32_t);
    std::vector<unsigned char> buffer(prepacked_data.begin(), prepacked_data.end());
    buffer.resize(buffer_size, 0);

    uint32_t* nonce_ptr = reinterpret_cast<uint32_t*>(&buffer[prepacked_data.size()]);
    *nonce_ptr = thread_id;

    int full_bytes = difficulty / 2;
    int remaining_nibble = difficulty % 2;

    while (!block_mined.load(std::memory_order_relaxed)) {
        // Clone the base context
        EVP_MD_CTX* ctx = EVP_MD_CTX_new();
        if (!ctx) {
            EVP_MD_CTX_free(base_ctx);
            throw std::runtime_error("Failed to create EVP_MD_CTX for iteration");
        }

        if (EVP_MD_CTX_copy_ex(ctx, base_ctx) != 1) {
            EVP_MD_CTX_free(ctx);
            EVP_MD_CTX_free(base_ctx);
            throw std::runtime_error("Failed to copy EVP_MD_CTX");
        }

        if (EVP_DigestUpdate(ctx, nonce_ptr, sizeof(uint32_t)) != 1) {
            EVP_MD_CTX_free(ctx);
            EVP_MD_CTX_free(base_ctx);
            throw std::runtime_error("Failed to update digest with nonce");
        }

        unsigned char hash[EVP_MAX_MD_SIZE];
        unsigned int length = 0;
        if (EVP_DigestFinal_ex(ctx, hash, &length) != 1) {
            EVP_MD_CTX_free(ctx);
            EVP_MD_CTX_free(base_ctx);
            throw std::runtime_error("Failed to finalize digest");
        }

        EVP_MD_CTX_free(ctx);
        thread_ops++;

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

        if (valid) {
            uint32_t found_nonce = *nonce_ptr;
            if (!block_mined.exchange(true)) {
                result_nonce.store(found_nonce);
                std::string hash_hex = hash_to_hex(hash, length);
                {
                    std::lock_guard<std::mutex> lock(output_mutex);
                    std::cout << "Thread " << thread_id << " mined the block with nonce: " 
                              << found_nonce << ", Hash: " << hash_hex << std::endl;
                }
            }
            break;
        }

        *nonce_ptr += num_threads;
    }

    EVP_MD_CTX_free(base_ctx);
}

// Exposed function for Python
extern "C" const char* mine_blocks(const char* prepacked_data_c, int data_length, int difficulty, int num_threads) {
    static std::string result; // Persistent result to return to Python
    block_mined.store(false);  // Reset the mined flag

    std::atomic<int> result_nonce(-1);
    int total_ops = 0;
    std::vector<std::thread> threads;
    std::vector<int> thread_ops(num_threads, 0); // Individual thread operations

    // Construct prepacked_data with the correct length
    std::string prepacked_data(prepacked_data_c, data_length);

    // Log the prepacked data for verification
    std::ostringstream oss;
    for (size_t i = 0; i < prepacked_data.size(); ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (int)(unsigned char)prepacked_data[i];
    }
    std::cout << "Prepacked Data (C++): " << oss.str() << std::endl;

    auto start_time = std::chrono::high_resolution_clock::now();

    // Launch threads
    for (int i = 0; i < num_threads; ++i) {
        threads.emplace_back(mine_block, std::ref(prepacked_data), difficulty, i, num_threads, 
                             std::ref(result_nonce), std::ref(thread_ops[i]));
    }

    // Join threads
    for (auto& t : threads) {
        t.join();
    }

    auto end_time = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> elapsed_time = end_time - start_time;

    // Calculate total operations and speed
    for (int i = 0; i < num_threads; ++i) {
        total_ops += thread_ops[i];
    }

    double speed = total_ops / elapsed_time.count();
    result = "Nonce: " + std::to_string(result_nonce.load()) + 
             ", Time Taken: " + std::to_string(elapsed_time.count()) + " seconds, " +
             "Speed: " + std::to_string(speed) + " ops/sec";

    return result.c_str();
}

extern "C" const char* calculate_block_hash(const char* prepacked_data_c, int data_length, uint32_t nonce) {
    static std::string result; // Persistent result for return
    std::string prepacked_data(prepacked_data_c, data_length);

    // Append the nonce to the prepacked data
    const size_t buffer_size = prepacked_data.size() + sizeof(uint32_t);
    std::vector<unsigned char> buffer(prepacked_data.begin(), prepacked_data.end());
    buffer.resize(buffer_size, 0);
    uint32_t* nonce_ptr = reinterpret_cast<uint32_t*>(&buffer[prepacked_data.size()]);
    *nonce_ptr = nonce;

    // Initialize OpenSSL hashing
    EVP_MD_CTX* ctx = EVP_MD_CTX_new();
    if (!ctx) {
        throw std::runtime_error("Failed to create EVP_MD_CTX");
    }

    unsigned char hash[EVP_MAX_MD_SIZE];
    unsigned int length = 0;

    if (EVP_DigestInit_ex(ctx, EVP_sha256(), nullptr) != 1 ||
        EVP_DigestUpdate(ctx, buffer.data(), buffer.size()) != 1 ||
        EVP_DigestFinal_ex(ctx, hash, &length) != 1) {
        EVP_MD_CTX_free(ctx);
        throw std::runtime_error("Failed to calculate hash");
    }

    EVP_MD_CTX_free(ctx);

    // Convert hash to hex string
    std::ostringstream oss;
    for (size_t i = 0; i < length; ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    result = oss.str();
    return result.c_str();
}