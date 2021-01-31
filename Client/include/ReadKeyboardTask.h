//
// Created by shir on 12/26/18.
//

#ifndef BOOST_ECHO_CLIENT_READKEYBOARDTASK_H
#define BOOST_ECHO_CLIENT_READKEYBOARDTASK_H

#include "connectionHandler.h"

class ReadKeyboardTask {
private:
    ConnectionHandler &connectionHandler;
public:
    ReadKeyboardTask(ConnectionHandler &ch);

    void operator()();
    virtual ~ReadKeyboardTask();//destructor
};
#endif //BOOST_ECHO_CLIENT_READKEYBOARDTASK_H
