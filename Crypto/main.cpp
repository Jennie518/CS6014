#include <iostream>
#include <array>
#include <vector>
#include <algorithm> // for std::shuffle
#include <random> // for std::default_random_engine
using Block = std::array<uint8_t, 8>; // 64-bit (8 bytes) block 64位（8字节）的块
using Key = Block; // 64-bit key 64位密钥
using SubstitutionTable = std::vector<std::byte>; //sub table is a vector of bytes 替换表是字节向量
using SubstitutionTables = std::vector<std::vector<std::byte>>; //vector of sub-tables 替换表的向量
#define MAX_TABLE_SIZE 256  //a byte can represent 2^8 = 256 different values 一个字节可以表示2^8=256个不同的值

// Function that generates a key based on the password 基于密码生成密钥的函数
Key generateKey(const std::string& password) {
    // Initiate key- 8 bytes array filled w 0's 初始化密钥-用0填充的8字节数组
    Key key = {0, 0, 0, 0, 0, 0, 0, 0};
    // loop over each char in password 遍历密码中的每个字符
    for (size_t i = 0; i < password.length(); ++i) {
        // key[i mod 8] = key[i mod 8] xor password[i] 密钥[i mod 8] = 密钥[i mod 8] 异或 密码[i]
        key[i % 8] ^= password[i];
    }
    return key;
}

// Helper function to randomly shuffles byte table using the Fisher-Yates shuffle algorithm
// 使用Fisher-Yates洗牌算法随机洗牌字节表的辅助函数
void shuffleTable(SubstitutionTable& table) {
    // Loop through the table in reverse order: 逆序遍历表：
    for (int i = MAX_TABLE_SIZE -1; i > 0; i--) {
        // For each i from 255 down to 1, generate a random index j between 0 and i
        // 对于每个i从255减到1，生成一个介于0和i之间的随机索引j
        int j = rand() % (i + 1);
        // Swap the elements at indices i and j 交换索引i和j处的元素
        std::swap(table[i], table[j]);
    }
}

SubstitutionTable createDecryptTable(const SubstitutionTable& encryptTable) {
    // initialize decryption array 初始化解密数组
    SubstitutionTable decryptTable(MAX_TABLE_SIZE);

    // Iterate over all 256 possible byte values. 遍历所有256个可能的字节值。
    for (int i = 0; i < MAX_TABLE_SIZE; ++i) {
        // Get the encrypted value at index i 获取索引i处的加密值
        int encryptedValue = static_cast<unsigned char>(encryptTable[i]); // needs to be cast to an integer to be used as an index 需要转换为整数才能用作索引
        decryptTable[encryptedValue] = static_cast<std::byte>(i);
    }
    return decryptTable;
}

void createSubstitutionTables(SubstitutionTables& encryptionTables, SubstitutionTables& decryptionTables) {
    // Seed random number generator with current time 使用当前时间种子随机数生成器
    srand(static_cast<unsigned>(time(nullptr)));

    // Initialize and fill the unshuffled table with 256 bytes (0-255) 初始化并填充未洗牌的表，包含256个字节（0-255）
    SubstitutionTable unshuffledTable(MAX_TABLE_SIZE);
    for (int i = 0; i < MAX_TABLE_SIZE; i++) {
        unshuffledTable[i] = static_cast<std::byte>(i);
    }

    encryptionTables.push_back(unshuffledTable); // First table (of 8 tables) is unshuffled 第一个表（共8个表）未洗牌
    decryptionTables.push_back(unshuffledTable); // Corresponding decryption table 对应的解密表

    // Generate 7 shuffled substitution tables 生成7个洗牌的替换表
    for (int i = 1; i <= 7; ++i) {
        SubstitutionTable shuffledTable = unshuffledTable;
        shuffleTable(shuffledTable);
        // Add encryption table (shuffled table) to list of encryption tables 将加密表（洗牌表）添加到加密表列表
        encryptionTables.push_back(shuffledTable);
        // Reverse the encryption table to create the decryption table, add the decryption table to list of decryption tables
        // 反转加密表以创建解密表，将解密表添加到解密表列表
        decryptionTables.push_back(createDecryptTable(shuffledTable));
    }
}
    Block encryptMessage(std::string stringMessage, SubstitutionTables& encryptionTables, Key key) {
        // initialize the state by converting message into a block (8 byte [])
        // 通过将消息转换为块（8字节[]）来初始化状态
        Block message;
        for (int i = 0; i < message.size(); ++i) {
            message[i] = stringMessage[i];
        }

        // encryption
        // Repeat for 16 rounds
        for (int i = 0; i < 16; i++) {
            // 1.) XOR every byte of the message with the key
            // 1.) 将消息的每个字节与密钥进行XOR操作
            for (int j = 0; j < message.size(); j++) {
                message[j] = message[j] ^ key[j];
            }
            // 2.) substitution table
            // Iterate over each byte in message and set its value to the same index in the kth encryption table
            // 遍历消息中的每个字节，并将其值设置为第k个加密表中的相同索引
            for (int k = 0; k < message.size(); k++) {
                int encryptionTableIndex = message[k];
                message[k] = (char) encryptionTables[k][encryptionTableIndex];
            }
            // 3.) rotation (bitwise); shift the whole state 1 bit to the left (wrap around)
            // 3.) 旋转（位操作）；将整个状态左移1位（循环）
            Block tempBlock = message; // a.) Copy the Block 复制块
            for (int l = 0; l < message.size(); ++l) {
                tempBlock[l] = (message[l] & 0x80) >> 7;
                message[l] = (message[l] << 1) & 0xFE;
            }
            // c.) Restore the LMB to the Last Byte 将LMB恢复到最后一个字节
            message[7] = message[7] | tempBlock[0];
            // d.) Shift the LMB right across the message 将LMB右移穿过消息
            for (int m = 0; m < message.size() - 1; ++m) {
                message[m] = message[m] | tempBlock[m + 1];
            }
        }
        return message;
    }

// Function to decrypt string message. Takes the message, list of decryption tables, and a key as parameters.
// 解密字符串消息的函数。采用消息、解密表列表和密钥作为参数。
    void decryptMessage(Block& message, SubstitutionTables& decryptionTables, Key key) {
        for (int i = 0; i < 16; i++) {
            // decryption 解密
            // 1.) rotate state right (bitwise) - reverse of encryption
            // 1.) 右旋状态（位操作）-加密的反向操作
            Block tempBlockDecrypt = message;
            for (int j = 0; j < message.size(); ++j) {
                tempBlockDecrypt[j] = (message[j] & 0x01) << 7;
                message[j] = (message[j] >> 1) & 0x7F;
            }
            message[0] = message[0] | tempBlockDecrypt[7];
            for (int k = 1; k < message.size(); ++k) {
                message[k] = message[k] | tempBlockDecrypt[k - 1];
            }
            // 2.) substitution table 同加密但使用解密表
            for (int l = 0; l < message.size(); l++) {
                message[l] = (char)decryptionTables[l][message[l]];
            }
            // 3.) XOR every byte of the message with the key 将消息的每个字节与密钥进行XOR操作
            for (int m = 0; m < message.size(); m++) {
                message[m] = message[m] ^ key[m];
            }
        }
    }

// Prints the contents of a Block 打印块的内容
    void printBlock(const Block message) {
        for (int i = 0; i < message.size(); ++i) {
            std::cout << message[i];
        }
        std::cout << std::endl;
    }

    int main() {
        // Generate key 生成密钥
        std::string password = "myneckmybacklickmypussyandmycrack";
        Key key = generateKey(password);

        // Create substitution tables for encryption and decryption
        // (will hold 8 substitution tables)
        // 创建加密和解密的替换表（将包含8个替换表）
        SubstitutionTables encryptionTables;
        SubstitutionTables decryptionTables;
        createSubstitutionTables(encryptionTables, decryptionTables);

        // Message to encrypt 待加密消息
        std::string stringMessage = "Jennie";
        std::cout << "Message before encryption: " << stringMessage << "\n";

        // Encrypt message 加密消息
        Block message = encryptMessage(stringMessage, encryptionTables, key);
        std::cout << "Message after encryption: ";
        printBlock(message);

        // commented out line shows what happens when a single bit is flipped
        // 注释掉的行展示了当翻转一个比特时会发生什么
        // message[5] = message[5] ^ 0x40;

        // Decrypt message 解密消息
        decryptMessage(message, decryptionTables, key);
        std::cout << "Message after decryption: ";
        printBlock(message);
        return 0;
    }
