#include <iostream>

class Singleton {
    std::string str;
    Singleton(std::string str) {
        this->str = str;
    }
public:
    static Singleton *getInstance() {
        static Singleton *instance1 = new Singleton("Singleton");
        return instance1;
}
    
    std::string getStr() {
        return this->str;
    }
};



int main(int argc, const char * argv[]) {

    Singleton *instance1 = Singleton::getInstance();
    std::cout << "String value: "<< instance1->getStr() << std::endl;
    
    Singleton *instance2 = Singleton::getInstance();
    std::cout << "String value: "<< instance2->getStr() << std::endl;
    
    //we just created two instances instance1 and instance2, but are they the same?
    
    return 0;
}
