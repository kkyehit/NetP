app.out: control.o streaming.o
control.o: 
	gcc -o controlServer controlServer.c -lpthread -lmysqlclient
streaming.o:
	gcc -o streamingServer streamingServer.c -lpthread -lmysqlclient
