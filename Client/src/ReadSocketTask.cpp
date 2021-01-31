
#include <connectionHandler.h>
#include <ReadSocketTask.h>

//private:
   // ConnectionHandler &connectionHandler;
//public:
    ReadSocketTask::ReadSocketTask(ConnectionHandler &ch) : connectionHandler(ch),shouldTerminate(false) {}

    void ReadSocketTask::operator()() {
        int len;
        while(!shouldTerminate){
            // We can use one of three options to read data from the server:
            // 1. Read a fixed number of characters
            // 2. Read a line (up to the newline character using the getline() buffered reader
            // 3. Read up to the null character
            std::string answer;
            // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
            // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
            if (!connectionHandler.getLine(answer)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }

            len=(int)answer.length();
            // A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
            // we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
          //  std::cout << answer << std::endl;
   //         std::cout << "Reply: " << answer << " " << len << " bytes " << std::endl << std::endl;
            answer.erase(answer.find_last_not_of(" \n\r\t")+1);
            if (answer == "ACK 3") {///get ack of logout
                //std::cout << "Exiting...\n" << std::endl;
                //break;
                connectionHandler.setShouldTerminate(true);
                // Release while
                connectionHandler.setLoginConsider(false);
                connectionHandler.close();
                break;
            }
            // Release while
            connectionHandler.setLoginConsider(false);
        }
    }

ReadSocketTask::~ReadSocketTask()= default;