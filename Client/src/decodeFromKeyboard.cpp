//
// Created by heni@wincs.cs.bgu.ac.il on 12/28/18.
//
#include <string>
#include "../include/decodeFromKeyboard.h"

using  namespace std;

decodeFromKeyboard::decodeFromKeyboard()
{}

void decodeFromKeyboard::parser(string commend , ConnectionHandler &connectionHandler ) {

    unsigned int firstArg = commend.find_first_of(' ');
    std::string Opcode = commend.substr(0, firstArg);
    std::string commendNonOP = commend.substr(firstArg + 1);
    if (Opcode.compare("REGISTER")==0)
        return parseMessage(commendNonOP, 1, 3 ,connectionHandler);
    else if (Opcode.compare("LOGIN")==0)
        return parseMessage(commendNonOP, 2, 3,connectionHandler);
    else if (Opcode.compare("LOGOUT")==0)
        return parseMessage("", 3, 2,connectionHandler);
    else if (Opcode.compare("FOLLOW")==0)
         parseFollow(commendNonOP, connectionHandler );
    else if (Opcode.compare("POST")==0){
        int size = commend.length()-2;
        char output [size];
        shortToBytes(5 ,output);
        for(unsigned int i = 0 ; i < commendNonOP.length() ; i++){
            output[i+2] = commendNonOP[i];
        }
        output[size-1] ='\0'; // put 0 in the end
        if (!connectionHandler.sendBytes(output , size)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;

        }
    }
    else if (Opcode.compare("PM")==0) {
        int size = commend.length();
        char output[size+1];
        shortToBytes(6, output);

        int firstSpace = commendNonOP.find_first_of(' ');
        commendNonOP.at(firstSpace)='\0';
        for (unsigned int i = 0; i < commendNonOP.length(); i++) {
            output[i + 2] = commendNonOP[i];
        }
        output[size - 1] = '\0'; // put 0 in the end
        if (!connectionHandler.sendBytes(output, size)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
        }
    }

    else if (Opcode.compare("USERLIST")==0) {
        char output[2];
        shortToBytes(7 ,output);
        if (!connectionHandler.sendBytes(output, 2))
            std::cout << "Disconnected. Exiting...\n" << std::endl;

    }
    else if (Opcode.compare("STAT")==0)
        return parseMessage(commendNonOP , 8 , 3 ,connectionHandler);

}


void decodeFromKeyboard::parseMessage(string commend, int op, int numOfZero , ConnectionHandler &connectionHandler){//numOfZero - two for the op and one for the end

    char output [commend.length() + numOfZero ];
    shortToBytes(op ,output);
    for(unsigned int i = 0 ; i < commend.length() ; i++){
        if(commend[i] == ' '){//where there is space - put 0
            output[i+2] = '\0';
        }
        else output[i+2] = commend[i];

    }

    if (op != 3 && op != 7) {
        output[commend.length() + numOfZero - 1] = '\0'; // put 0 in the end
    }
    if (!connectionHandler.sendBytes(output , commend.length() + numOfZero)) {
        std::cout << "Disconnected. Exiting...\n" << std::endl;

    }


}



void decodeFromKeyboard::parseFollow(string commend, ConnectionHandler &connectionHandler){

    char opArray [2];//array for the opCode
    shortToBytes(4, opArray);

    int firstSpace = commend.find_first_of(' ');
    string follow = commend.substr(0 ,firstSpace);// find the follow/unfollow bytes
    char followArray [1];
    if (follow=="0")
        followArray[0]=0;
    else if (follow=="1")
        followArray[0]=1;
    commend = commend.substr(firstSpace+1);// commend without follow bytes
    
    int secondSpace = commend.find_first_of(' ');
    string numOfUsers = commend.substr(0, secondSpace);
    char numOfUsersArray [2];
    shortToBytes(std::stoi(numOfUsers) , numOfUsersArray);
    commend = commend.substr(secondSpace+1);//commend without numOfUsers bytes


    string UsersNameList = commend;
    char  usersNameArray [UsersNameList.length()+1];
    for(unsigned int i = 0 ; i < UsersNameList.length() ; i++){
        if(UsersNameList[i] == ' '){//where there is space - put 0
            usersNameArray[i] = '\0';
        }
        else usersNameArray[i] = UsersNameList[i];

    }
    usersNameArray[UsersNameList.length()] ='\0'; // put 0 in the end

    char output [6+ UsersNameList.length()];//array that combines all the arrays
    //insert the opcode
    output[0] = opArray[0];
    output[1] = opArray[1];

    //insert the follow/unfollow
    output[2] = followArray [0];

    //insert the numOfUser
    output[3] = numOfUsersArray[0];
    output[4] = numOfUsersArray[1];

    //insert the usersNameList
    for (unsigned int i = 0 ; i < UsersNameList.length()+1 ; i++)
        output[i+5] = usersNameArray[i];

    if (!connectionHandler.sendBytes(output , 6+ UsersNameList.length())) {
        std::cout << "Disconnected. Exiting...\n" << std::endl;
    }

}



void decodeFromKeyboard::shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}