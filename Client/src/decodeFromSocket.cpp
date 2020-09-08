//
// Created by heni@wincs.cs.bgu.ac.il on 12/29/18.
//

#include "../include/connectionHandler.h"
#include "../include/decodeFromSocket.h"
using namespace std;


decodeFromSocket::decodeFromSocket(ConnectionHandler &CH ,bool *terminateFromMain, bool *gotLogoutFromMain) : ch(CH) , terminate(terminateFromMain) , gotLogout(gotLogoutFromMain) { }

void decodeFromSocket::run(){

    char answer [2];
    while (!*terminate){
        ch.ConnectionHandler::getBytes(answer , 2); // read two bytes - op
         decode(*answer);
    }




}

void decodeFromSocket::decode(char & ans){

    int opCode = bytesToShort(&ans);
    switch (opCode) {
        case 9 : {
            string opCodeString = "NOTIFICATION";
            char NotificationType[1];
            ch.getBytes(NotificationType, 1);
            int numOfNotification = bytesToShort(NotificationType);
            string NotificationToPrint;
            if (numOfNotification == 0)
                NotificationToPrint = "PM";
            else NotificationToPrint = "Public";
            string PostingUser;
            ch.getLine(PostingUser);
            PostingUser.resize(PostingUser.size()-1);
            string content;
            ch.getLine(content);
            content.resize(content.length()-1);

            cout << opCodeString + " " + NotificationToPrint + " " + PostingUser + " " + content<< endl;
            break;
        }

        case 10: {
            string opCodeString = "ACK";
            char MessageOpCode[2];
            ch.getBytes(MessageOpCode, 2);
            int MessageOP = bytesToShort(MessageOpCode);
            switch (MessageOP) {
                case 1 : {
                    cout << opCodeString + " " + std::to_string(MessageOP) << endl;
                    break;
                }
                case 2:{
                    cout << opCodeString + " " + std::to_string(MessageOP)<<endl;
                    break;
                }
                    case 3: {
                        cout << opCodeString + " " + std::to_string(MessageOP)<<endl;
                    *terminate = true;
                    *gotLogout = true;
                       break;
                }
                case 4: {
                    char NumOfUsers [2];
                    ch.getBytes(NumOfUsers , 2);
                    unsigned int NumUsers = bytesToShort(NumOfUsers);
                    string UserNameList;
                   for(unsigned int i = 0 ; i < NumUsers ; i++ ){
                       string userName;
                       ch.getLine(userName);
                       int len = userName.length();
                       userName.resize(len-1);
                       UserNameList = UserNameList + userName +" " ;
                   }
                    cout << opCodeString + " " + std::to_string(MessageOP) + " " + to_string(NumUsers) + " " + UserNameList<< endl;
                    break;
                }
                case 5 :{
                    cout << opCodeString + " " + std::to_string(MessageOP)<<endl;
                    break;
                }
                case 6 :{
                    cout << opCodeString + " " + std::to_string(MessageOP)<<endl;
                    break;

                }
                case 7 : {
                    char NumOfUsers [2];
                    ch.getBytes(NumOfUsers , 2);
                    unsigned int NumUsers = bytesToShort(NumOfUsers);
                    string UserNameList;
                    for(unsigned int i = 0 ; i < NumUsers ; i++ ){
                        string userName;
                        ch.getLine(userName);
                        int len = userName.length();
                        userName.resize(len-1);//CHECK IF NEEDED
                        UserNameList = UserNameList + userName +" " ;
                    }
                    cout << opCodeString + " " + std::to_string(MessageOP) + NumOfUsers + " " + UserNameList<< endl;
                    break;

                }
                case 8 : {
                    char NumPosts [2];
                    ch.getBytes(NumPosts, 2);
                    int numPostToPrint = bytesToShort(NumPosts);
                    char NumFollowers [2];
                    ch.getBytes(NumFollowers, 2);
                    int NumFollowersToPrint = bytesToShort(NumFollowers);
                    char NumFollowing [2];
                    ch.getBytes(NumFollowing, 2);
                    int NumFollowingToPrint = bytesToShort(NumFollowing);

                    cout << opCodeString + " " + std::to_string(MessageOP) + " " + std::to_string(numPostToPrint) + " " + std::to_string(NumFollowersToPrint) + " " + std::to_string( NumFollowingToPrint) << endl;
                    break;
                }
            }
            break;
        }
        case 11 : {
            char MessageOpCode[2];
            ch.getBytes(MessageOpCode, 2);
            int MessageOP = bytesToShort(MessageOpCode);
            if (MessageOP == 3)
                *gotLogout = true;
            cout<< "ERROR: " + std::to_string(MessageOP) << endl;
            break;
        }
        }
}


short decodeFromSocket::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}









