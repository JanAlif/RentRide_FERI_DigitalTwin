#include <stdint.h>
#include <cstring>
#include <openssl/evp.h>
#include <openssl/err.h>
#include <string>
#include <vector>

// Function to calculate SHA-256 hash using EVP interface
std::string sha256(const std::string& data) {
    unsigned char hash[EVP_MAX_MD_SIZE];
    unsigned int hash_len = 0;

    EVP_MD_CTX* mdctx = EVP_MD_CTX_new();
    if (mdctx == nullptr) {
        // Handle error
        return "";
    }

    if (1 != EVP_DigestInit_ex(mdctx, EVP_sha256(), nullptr)) {
        // Handle error
        EVP_MD_CTX_free(mdctx);
        return "";
    }

    if (1 != EVP_DigestUpdate(mdctx, data.c_str(), data.size())) {
        // Handle error
        EVP_MD_CTX_free(mdctx);
        return "";
    }

    if (1 != EVP_DigestFinal_ex(mdctx, hash, &hash_len)) {
        // Handle error
        EVP_MD_CTX_free(mdctx);
        return "";
    }

    EVP_MD_CTX_free(mdctx);

    char outputBuffer[65];
    for(unsigned int i = 0; i < hash_len; ++i)
        snprintf(outputBuffer + (i * 2), sizeof(outputBuffer) - (i * 2), "%02x", hash[i]);
    outputBuffer[64] = 0;
    return std::string(outputBuffer);
}

// Exported mining function
extern "C" {

// Structure to hold mining parameters
struct MiningParams {
    char prepacked_data[1024]; // Adjust size as needed
    int difficulty;
    uint32_t start_nonce;
    int step;
};

// Structure to hold mining result
struct MiningResult {
    uint32_t nonce;
    char hash[65];
};

// Mining function
MiningResult mine_block(const MiningParams* params) {
    MiningResult result;
    uint32_t nonce = params->start_nonce;
    std::string data(params->prepacked_data, 1024);
    int difficulty = params->difficulty;
    std::string target_prefix(difficulty, '0');

    while (true) {
        std::string data_with_nonce = data + std::to_string(nonce);
        std::string hash = sha256(data_with_nonce);
        if (hash.substr(0, difficulty) == target_prefix) {
            result.nonce = nonce;
            strncpy(result.hash, hash.c_str(), 64);
            result.hash[64] = '\0';
            return result;
        }
        nonce += params->step;
    }
}

}