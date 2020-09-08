CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: Main
	g++ -o bin/BGSclient bin/connectionHandler.o bin/decodeFromKeyboard.o bin/decodeFromSocket.o bin/Main.o $(LDFLAGS)

Main: bin/connectionHandler.o bin/decodeFromKeyboard.o bin/decodeFromSocket.o bin/Main.o

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/decodeFromKeyboard.o: src/decodeFromKeyboard.cpp
	g++ $(CFLAGS) -o bin/decodeFromKeyboard.o src/decodeFromKeyboard.cpp

bin/decodeFromSocket.o: src/decodeFromSocket.cpp
	g++ $(CFLAGS) -o bin/decodeFromSocket.o src/decodeFromSocket.cpp

bin/Main.o: src/Main.cpp
	g++ $(CFLAGS) -o bin/Main.o src/Main.cpp

.PHONY: clean
clean:
	rm -f bin/*