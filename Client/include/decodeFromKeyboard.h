//
// Created by heni@wincs.cs.bgu.ac.il on 12/28/18.
//

#ifndef BOOST_ECHO_CLIENT_DECODEFROMKEYBOARD_H
#define BOOST_ECHO_CLIENT_DECODEFROMKEYBOARD_H

#endif //BOOST_ECHO_CLIENT_DECODEFROMKEYBOARD_H
#include "connectionHandler.h"
class decodeFromKeyboard{
    public:
    decodeFromKeyboard();
    void parser(std::string commend , ConnectionHandler &connectionHandler);

    private:

    void shortToBytes(short num, char* bytesArr);
    void parseMessage(std::string commend, int op, int numOfZero , ConnectionHandler &connectionHandler);
    void parseFollow(std::string commend,  ConnectionHandler &connectionHandler);

};