#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__

#include <string>
#include <iostream>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
    const std::string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;
    bool shouldTerminate;
    bool loginConsider;

public:
    ConnectionHandler(std::string host, short port);


    void setLoginConsider(bool con);

    bool getLoginConsider();

    virtual ~ConnectionHandler();

    // Connect to the remote machine
    bool connect();

    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);

    // Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);

    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string &line);

    // Send an ascii line from the server (probably should be from client)
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string &line);

    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string &frame, char delimiter);

    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string &frame, char delimiter);

    // Close down the connection properly.
    void close();

    bool decode(std::string &line);

    short bytesToShort(char *bytesArr);

    void shortToBytes(short num, char *bytesArr);

    std::vector<char> encode(std::string s);

    void moveToBytesPost(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans);

    void moveToBytesRegisterLogin(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans);

    void moveToBytesFollow(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans);

    void moveToBytesPM(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans);

    void moveToBytesSTAT(char opCode[2], std::vector<std::string> &tokens, std::vector<char> &ans);

    short popOneBytesToShort(char *bytesArr);

    bool decodeForNotification(std::string &line);

    bool decodeForAck(std::string &line);

    bool decodeForError(std::string &line);

    bool getShouldTerminate();

    void setShouldTerminate(bool b);

}; //class ConnectionHandler

#endif