#include <connectionHandler.h>
#include <boost/algorithm/string.hpp>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_) {

}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        //address is a address and a port
        //remote host - server
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        //connect the socket to the endpoint address
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception &e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {
            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLoginConsider()
{
    return loginConsider;
}

void ConnectionHandler::setLoginConsider(bool con) {
    loginConsider = con;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string &line) {
    //  return getFrameAscii(line, '\n');
    return decode(line);
}

bool ConnectionHandler::sendLine(std::string &line) {
    std::vector<char> ans = encode(line);
    int size = ans.size();
    char *output = ans.data();
    return sendBytes(output, size);
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

bool ConnectionHandler::decode(std::string &line) {
    char c;
    getBytes(&c, 2);
    string s = std::to_string(bytesToShort(&c));
    if (s == "9") { //notification
        return decodeForNotification(line);
    } else if (s == "10") { //ACK
        return decodeForAck(line);
    } else if (s == "11") { //error
        return decodeForError(line);
    }
}

std::vector<char> ConnectionHandler::encode(std::string s) {

    std::vector<std::string> tokens;
    std::vector<char> ans;
    char opCode[2];

    boost::split(tokens, s, boost::is_any_of(" ")); // function of boost which splits the string by delimiter

    if (tokens.front() == "REGISTER" || tokens.front() == "LOGIN") {
        shortToBytes((short) (tokens.front() == "REGISTER" ? 1 : 2), opCode);
        moveToBytesRegisterLogin(opCode, tokens, ans);
    } else if (tokens.front() == "LOGOUT") {
        shortToBytes(3, opCode);
        ans.push_back(opCode[0]);
        ans.push_back(opCode[1]);
    } else if (tokens.front() == "FOLLOW") {
        shortToBytes(4, opCode);
        moveToBytesFollow(opCode, tokens, ans);
    } else if (tokens.front() == "POST") {
        shortToBytes(5, opCode);
        moveToBytesPost(opCode, tokens, ans);
    } else if (tokens.front() == "PM") {
        shortToBytes(6, opCode);
        moveToBytesPM(opCode, tokens, ans);
    } else if (tokens.front() == "USERLIST") {
        shortToBytes(7, opCode);
        ans.push_back(opCode[0]);
        ans.push_back(opCode[1]);
    } else if (tokens.front() == "STAT") {
        shortToBytes(8, opCode);
        moveToBytesSTAT(opCode, tokens, ans);
    }
    return ans;
}

void ConnectionHandler::moveToBytesPost(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans) {
    ans.push_back(opCode[0]);//opcode
    ans.push_back(opCode[1]);

    for (int i = 1; i < tokens.size(); i++) { //for each word in content
        const char *c = (tokens.at(i).c_str());

        for (int j = 0; j < strlen(c); j++) {
            ans.push_back(c[j]);
        }
        if (i < (tokens.size() - 1)) {
            char cha = ' ';
            ans.push_back(cha);
        }
    }
    ans.push_back('\0');
}

void ConnectionHandler::moveToBytesSTAT(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans) {
    ans.push_back(opCode[0]);//opCode
    ans.push_back(opCode[1]);

    const char *c = (tokens.at(1).c_str()); //userName
    for (int j = 0; j < strlen(c); j++) {
        ans.push_back(c[j]);
    }

    ans.push_back('\0');

}

void ConnectionHandler::moveToBytesPM(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans) {
    ans.push_back(opCode[0]);//opCode
    ans.push_back(opCode[1]);

    int size = tokens.size();

    const char *c = (tokens.at(1).c_str()); //userName
    for (int j = 0; j < strlen(c); j++) {
        ans.push_back(c[j]);
    }

    ans.push_back('\0');

    for (int i = 2; i < size; i++) { //content
        const char *c1 = (tokens.at(i).c_str());

        for (int j = 0; j < strlen(c1); j++) {
            ans.push_back(c1[j]);
        }
        if (i < size - 1) {
            char c2 = ' ';
            ans.push_back(c2);
        }
    }
    ans.push_back('\0');
}

void ConnectionHandler::moveToBytesRegisterLogin(char opCode[2], std::vector<std::string> &tokens,
                                                 std::vector<char> &ans) {
    ans.push_back(opCode[0]);
    ans.push_back(opCode[1]);
    for (int i = 1; i < tokens.size(); i++) {
        const char *c = (tokens.at(i).c_str());
        for (int j = 0; j < strlen(c); j++) {
            ans.push_back(c[j]);
        }
        ans.push_back('\0');
    }
}

void ConnectionHandler::moveToBytesFollow(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans) {
    ans.push_back(opCode[0]);
    ans.push_back(opCode[1]);

    const char *c = (tokens.at(1).c_str()); //follow/unFollow
    ans.push_back(c[0]);

    short sh =  (short)std::stoi(tokens.at(2)); //Num of users
     char c2[2];
     shortToBytes(sh,c2);
     ans.push_back(c2[0]);
     ans.push_back(c2[1]);

    /*char c1 = ((sh >> 8) & 0xFF);;
    char c2 = (sh & 0xFF);
    ans.push_back(c1);
    ans.push_back(c2);*/
    for (int i = 3; i < tokens.size(); i++) { //UserNameList
        const char *c = (tokens.at(i).c_str()); //for each user name
        for (int j = 0; j < strlen(c); j++) {
            ans.push_back(c[j]);
        }
        ans.push_back('\0'); //between 2 users & in the end
    }
}


bool ConnectionHandler::decodeForNotification(std::string &line) {
    string str = "NOTIFICATION ";

    char ch;
    getBytes(&ch, 1);
    if (ch == '0') {
        str += "PM ";
    } else if (ch == '1') {
        str += "Public ";
    }

    while (ch != '\0') { //posting user
        getBytes(&ch, 1);
        if (ch != '\0') {
            str += ch;
        }
    }
    str += " ";
    ch = ' ';

    while (ch != '\0') { //content
        getBytes(&ch, 1);
        if (ch != '\0') {
            str += ch;
        }
    }

    line.append(str);
    std::cout << str << std::endl;
    return true;
}

bool ConnectionHandler::decodeForAck(std::string &line) { //TODO Add optional part
    string str = "ACK ";
    char ch[2];
    getBytes(ch, 2);
    string string1 = std::to_string(bytesToShort(ch));//command opCode
    str += string1;
    //userList ACK or Follow ACK
    if (string1==("4") || (string1 == ("7"))) {
        str += " ";
        getBytes(ch, 2);
        str += std::to_string(bytesToShort(ch)); //numOfUsers
        int num = (int) bytesToShort(ch);
        for (int i = 0; i < num; i++) { //for each userName
            str += " ";
            char ch1 = ' ';

            while (ch1 != '\0') { // read the name until \0
                getBytes(&ch1, 1);
                if(ch1 != '\0') {
                    str += ch1;
                }
            }

        }


    } else if (string1 == "8") { // stat ACK
        int j = 0;
        while (j < 3) { // 3 sets of 2 bytes : NumPosts, numFollowers, numFollowing
            str += " ";
            getBytes(ch, 2);
            str += std::to_string(bytesToShort(ch));
            j++;
        }
    }

    line.append(str);
    std::cout << str << std::endl;
    return true;
}

bool ConnectionHandler::decodeForError(std::string &line) {
    string str = "ERROR ";
    char ch5[2];
    getBytes(ch5, 2);
    str += std::to_string(bytesToShort(ch5)); //message opCode
    line.append(str);
    std::cout << str << std::endl;
    return true;
}


bool ConnectionHandler::sendFrameAscii(const std::string &frame, char delimiter) {
    bool result = sendBytes(frame.c_str(), frame.length());
    if (!result) return false;
    return sendBytes(&delimiter, 1);
}


bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) {
    char ch = ' ';
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do {
            getBytes(&ch, 1);
            frame.append(1, ch);
        } while (delimiter != ch);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}


void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = (char)((num >> 8) & 0xFF);
    bytesArr[1] = (char)(num & 0xFF);
}

bool ConnectionHandler::getShouldTerminate() {
    return shouldTerminate;
}

void ConnectionHandler::setShouldTerminate(bool b) {
    shouldTerminate = b;
}