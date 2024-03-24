#include <iostream>
#include <array>
#include <vector>
#include <algorithm> // for std::shuffle
#include <random> // for std::default_random_engine
using Block = std::array<uint8_t, 8>; // 64-bit (8 bytes) block
using Key = Block; // 64-bit key
using SubstitutionTable = std::vector<std::byte>; // Substitution table is a vector of bytes
using SubstitutionTables = std::vector<std::vector<std::byte>>; // Vector of substitution tables

#define MAX_TABLE_SIZE 256  // A byte can represent 2^8 = 256 different values

// Function that generates a key based on the password
Key generateKey(const std::string& password) {
    // Initialize key - 8 bytes array filled with 0's
    Key key = {0, 0, 0, 0, 0, 0, 0, 0};
    // Loop over each character in the password
    for (size_t i = 0; i < password.length(); ++i) {
        // key[i mod 8] = key[i mod 8] xor password[i]
        key[i % 8] ^= password[i];
    }
    return key;
}

// Helper function to randomly shuffle byte table using the Fisher-Yates shuffle algorithm
void shuffleTable(SubstitutionTable& table) {
    // Loop through the table in reverse order
    for (int i = MAX_TABLE_SIZE - 1; i > 0; i--) {
        // For each i from 255 down to 1, generate a random index j between 0 and i
        int j = rand() % (i + 1);
        // Swap the elements at indices i and j
        std::swap(table[i], table[j]);
    }
}

// Function to create a decryption table from an encryption table
SubstitutionTable createDecryptTable(const SubstitutionTable& encryptTable) {
    // Initialize decryption array
    SubstitutionTable decryptTable(MAX_TABLE_SIZE);

    // Iterate over all 256 possible byte values.
    for (int i = 0; i < MAX_TABLE_SIZE; ++i) {
        // Get the encrypted value at index i
        int encryptedValue = static_cast<unsigned char>(encryptTable[i]);
        decryptTable[encryptedValue] = static_cast<std::byte>(i);
    }
    return decryptTable;
}

// Function to create substitution tables for encryption and decryption
void createSubstitutionTables(SubstitutionTables& encryptionTables, SubstitutionTables& decryptionTables) {
    // Seed random number generator with current time
    srand(static_cast<unsigned>(time(nullptr)));

    // Initialize and fill the unshuffled table with 256 bytes (0-255)
    SubstitutionTable unshuffledTable(MAX_TABLE_SIZE);
    for (int i = 0; i < MAX_TABLE_SIZE; i++) {
        unshuffledTable[i] = static_cast<std::byte>(i);
    }

    encryptionTables.push_back(unshuffledTable); // First table (of 8 tables) is unshuffled
    decryptionTables.push_back(unshuffledTable); // Corresponding decryption table

    // Generate 7 shuffled substitution tables
    for (int i = 1; i <= 7; ++i) {
        SubstitutionTable shuffledTable = unshuffledTable;
        shuffleTable(shuffledTable);
        // Add encryption table (shuffled table) to list of encryption tables
        encryptionTables.push_back(shuffledTable);
        // Reverse the encryption table to create the decryption table, add the decryption table to list of decryption tables
        decryptionTables.push_back(createDecryptTable(shuffledTable));
    }
}

// Function to encrypt a string message
Block encryptMessage(std::string stringMessage, SubstitutionTables& encryptionTables, Key key) {
    // Initialize the state by converting message into a block (8 bytes array)
    Block message;
    for (int i = 0; i < message.size(); ++i) {
        message[i] = stringMessage[i];
    }

    // Encryption process (16 rounds)
    for (int i = 0; i < 16; i++) {
        // 1.) XOR every byte of the message with the key
        for (int j = 0; j < message.size(); j++) {
            message[j] = message[j] ^ key[j];
        }
        // 2.) Substitution table
        for (int k = 0; k < message.size(); k++) {
            int encryptionTableIndex = message[k];
            message[k] = static_cast<char>(encryptionTables[k][encryptionTableIndex]);
        }
        // 3.) Rotation (bitwise); shift the whole state 1 bit to the left (wrap around)
        Block tempBlock = message;
        for (int l = 0; l < message.size(); ++l) {
            tempBlock[l] = (message[l] & 0x80) >> 7;
            message[l] = (message[l] << 1) & 0xFE;
        }
        message[7] = message[7] | tempBlock[0];
        for (int m = 0; m < message.size() - 1; ++m) {
            message[m] = message[m] | tempBlock[m + 1];
        }
    }
    return message;
}

// Function to decrypt a message
void decryptMessage(Block& message, SubstitutionTables& decryptionTables, Key key) {
    // Decryption process (16 rounds)
    for (int i = 0; i < 16; i++) {
        // 1.) Rotate state right (bitwise)
        Block tempBlockDecrypt = message;
        for (int j = 0; j < message.size(); ++j) {
            tempBlockDecrypt[j] = (message[j] & 0x01) << 7;
            message[j] = (message[j] >> 1) & 0x7F;
        }
        message[0] = message[0] | tempBlockDecrypt[7];
        for (int k = 1; k < message.size(); ++k) {
            message[k] = message[k] | tempBlockDecrypt[k - 1];
        }
        // 2.) Substitution table (using decryption table)
        for (int l = 0; l < message.size(); l++) {
            message[l] = static_cast<char>(decryptionTables[l][message[l]]);
        }
        // 3.) XOR every byte of the message with the key
        for (int m = 0; m < message.size(); m++) {
            message[m] = message[m] ^ key[m];
        }
    }
}

// Prints the contents of a Block
void printBlock(const Block message) {
    for (int i = 0; i < message.size(); ++i) {
        std::cout << message[i];
    }
    std::cout << std::endl;
}

int main() {
    // Generate key
    std::string password = "myneckmybacklickmypussyandmycrack";
    Key key = generateKey(password);

    // Create substitution tables for encryption and decryption
    SubstitutionTables encryptionTables;
    SubstitutionTables decryptionTables;
    createSubstitutionTables(encryptionTables, decryptionTables);

    // Message to encrypt
    std::string stringMessage = "Jennie";
    std::cout << "Message before encryption: " << stringMessage << "\n";

    // Encrypt message
    Block message = encryptMessage(stringMessage, encryptionTables, key);
    std::cout << "Message after encryption: ";
    printBlock(message);

    // Decrypt message
    decryptMessage(message, decryptionTables, key);
    std::cout << "Message after decryption: ";
    printBlock(message);
    return 0;
}

