

#include <connectionHandler.h>
#include <ReadKeyboardTask.h>

    ReadKeyboardTask::ReadKeyboardTask(ConnectionHandler &ch) : connectionHandler(ch) {}

    void ReadKeyboardTask::operator()() {

        while (!connectionHandler.getShouldTerminate()) {//read Socket thread finish its work// change to socket is open
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            int len=line.length();

            line.erase(line.find_last_not_of(" \n\r\t")+1);

            if (!connectionHandler.sendLine(line)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }

            if (line == "LOGOUT") {
                connectionHandler.setLoginConsider(true);
                // Block...
                while(connectionHandler.getLoginConsider()){};
            }

            // connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
       //     std::cout << "Sent " << len+1 << " bytes to server" << std::endl;
        }

    }

ReadKeyboardTask::~ReadKeyboardTask()= default;