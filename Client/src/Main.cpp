#include <stdlib.h>
#include <thread>
#include "connectionHandler.h"
#include "../include/decodeFromSocket.h"
#include "../include/decodeFromKeyboard.h"
#include <mutex>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
      std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
       return -1;
   }


   std::string host = argv[1];
  short port = std::atoi(argv[2]);



    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    bool* terminate = new bool(false);
    bool* gotLogout = new bool(false);
	decodeFromKeyboard Rkeyboard;
    decodeFromSocket Rsocket(connectionHandler , terminate , gotLogout);

    std::thread readerFromSocket(&decodeFromSocket::run, &Rsocket);

    while (!*terminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);

        Rkeyboard.parser(line ,connectionHandler);

        if (line == "LOGOUT"){

            while (!(*gotLogout)){

            }
            *gotLogout = true;

        }

    }
    readerFromSocket.join();
    delete gotLogout;
    delete terminate;
    return 0;
}
