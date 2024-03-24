#include <iostream>

#include <vector>
#include <iostream>
#include <string>

class RC4 {
public:
    std::vector<int> S;
    int i = 0;
    int j = 0;

    RC4(const std::string& key) {
        S.resize(256);
        for (int k = 0; k < 256; ++k) S[k] = k;

        int j = 0;
        for (int i = 0; i < 256; ++i) {
            j = (j + S[i] + key[i % key.size()]) % 256;
            std::swap(S[i], S[j]);
        }
    }

    unsigned char getNextByte() {
        i = (i + 1) % 256;
        j = (j + S[i]) % 256;
        std::swap(S[i], S[j]);
        return S[(S[i] + S[j]) % 256];
    }

    std::vector<unsigned char> encryptDecrypt(const std::string& data) {
        std::vector<unsigned char> output(data.size());
        for (size_t k = 0; k < data.size(); ++k) {
            output[k] = data[k] ^ getNextByte();
        }
        return output;
    }
};

void printHex(const std::vector<unsigned char>& data) {
    for (unsigned char byte : data) {
        printf("%02hhx", byte);
    }
    std::cout << std::endl;
}

int main() {
    std::string key = "secret";
    std::string message = "Your salary is $1000";

    RC4 rc4(key);
    auto encrypted = rc4.encryptDecrypt(message);
    std::cout << "Encrypted: ";
    printHex(encrypted);

    RC4 rc4decrypt(key);
    auto decrypted = rc4decrypt.encryptDecrypt(std::string(encrypted.begin(), encrypted.end()));
    std::cout << "Decrypted: " << std::string(decrypted.begin(), decrypted.end()) << std::endl;

    return 0;
}
