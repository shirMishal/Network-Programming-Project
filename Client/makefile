CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

all: BGSclient
	g++ -o bin/BGSclient bin/connectionHandler.o bin/BGSclient.o bin/ReadKeyboardTask.o bin/ReadSocketTask.o $(LDFLAGS) 

BGSclient: bin/connectionHandler.o bin/BGSclient.o bin/ReadKeyboardTask.o bin/ReadSocketTask.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BGSclient.o: src/BGSclient.cpp
	g++ $(CFLAGS) -o bin/BGSclient.o src/BGSclient.cpp
	
bin/ReadKeyboardTask.o: src/ReadKeyboardTask.cpp
	g++ $(CFLAGS) -o bin/ReadKeyboardTask.o src/ReadKeyboardTask.cpp
	
bin/ReadSocketTask.o: src/ReadSocketTask.cpp
	g++ $(CFLAGS) -o bin/ReadSocketTask.o src/ReadSocketTask.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
