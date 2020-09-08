//
// Created by heni@wincs.cs.bgu.ac.il on 12/29/18.
//

#ifndef BOOST_ECHO_CLIENT_DECODEFROMSOCKET_H
#define BOOST_ECHO_CLIENT_DECODEFROMSOCKET_H

#endif //BOOST_ECHO_CLIENT_DECODEFROMSOCKET_H
#include "connectionHandler.h"

class decodeFromSocket{
public:
    decodeFromSocket(ConnectionHandler &CH , bool *terminate , bool *gotLogout);
    void run();
    void decode(char& ans);


private:
    ConnectionHandler &ch;
    short bytesToShort(char* bytesArr);
    bool * terminate;
    bool * gotLogout;
};

