#include <stdlib.h>
#include <connectionHandler.h>
#include <ReadKeyboardTask.h>
#include <thread>
#include <ReadSocketTask.h>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }


    ReadKeyboardTask keyboard(connectionHandler);//TODO check on send by ref... is it ok to send same connection handler to both task by ref
    ReadSocketTask socket (connectionHandler);

    std::thread th1(std::ref(keyboard)); // we use std::ref to avoid creating a copy of the Task object
    std::thread th2(std::ref(socket));
    th2.join();
    th1.join();

    return 0;
}
