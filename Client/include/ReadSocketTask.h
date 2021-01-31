//
// Created by shir on 12/26/18.
//

#ifndef BOOST_ECHO_CLIENT_READSOCKETTASK_H
#define BOOST_ECHO_CLIENT_READSOCKETTASK_H

#include "connectionHandler.h"


class ReadSocketTask {
private:
    ConnectionHandler &connectionHandler;
    bool shouldTerminate;
public:
    ReadSocketTask(ConnectionHandler &ch);

    void operator()();
    virtual ~ReadSocketTask();//destructor
};
#endif //BOOST_ECHO_CLIENT_READSOCKETTASK_H
