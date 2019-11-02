#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <fcntl.h>
#include <signal.h>
#include <string.h>
#include <sys/stat.h>
#include <errno.h>
#include "/usr/include/mysql/mysql.h"

#define MAX_UPLOAD_BUF 16384
#define MAX_DOWNLOAD_BUF 16834
#define MAX_MSG_BUF 256
#define MAX_TITLE 60
#define MAX_ID 10
#define MAX_URL 256
#define FILE_MODE 0644

struct media_data
{
	int size;
	int views;
	char key[4];
	char title[60];
	char id[10];
};

int port;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond = PTHREAD_COND_INITIALIZER;
pthread_mutex_t mutex2 = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond2 = PTHREAD_COND_INITIALIZER;

void *uploadThread(void *arg);
void *downloadThread(void *arg);
void *judgeThread(void *arg);
char *getMediaUrl(char *_key);								 // key 값 인자로 주면 해당 동영상 URL 리턴, 없으면 NULL 리턴
int putMediaData(struct media_data _data, char *_mediaName); //서버에 저장된 URL 주소, 올린사람 ID, 동영상 이름 저장
															 // 성공시 1, 실패시 -1 리턴

void control_signal(int signo);
int main(int argc, char *argv[])
{
	int listensock;
	struct sockaddr_in serveraddr, clientaddr;
	struct sigaction sigact;

	char readflag = -1;
	int addrsize = sizeof(clientaddr);
	pthread_t tid;

	if (argc != 2)
	{
		fprintf(stderr, "Usage : %s <port>\n", argv[0]);
		exit(1);
	}

	sigact.sa_handler = control_signal;
	sigact.sa_flags = 0;
	sigemptyset(&sigact.sa_mask);

	sigaction(SIGUSR1, &sigact, NULL);
	signal(SIGPIPE, SIG_IGN);

	port = atoi(argv[1]);

	if ((listensock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
	{
		fprintf(stderr, "listen error\n");
		exit(1);
	}

	memset(&serveraddr, 0, sizeof(serveraddr));
	serveraddr.sin_family = AF_INET;
	serveraddr.sin_port = htons(port);
	serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(listensock, (struct sockaddr *)&serveraddr, sizeof(serveraddr)) < 0)
	{
		fprintf(stderr, "bind error\n");
		exit(1);
	}
	if (listen(listensock, 1024) < 0)
	{
		fprintf(stderr, "listen error\n");
		exit(1);
	}
	printf("pid : %d\n", getpid());
	pthread_cond_signal(&cond);
	while (1)
	{
		int clientsock = accept(listensock, (struct sockaddr *)&clientaddr, &addrsize);
		if (clientsock < 0)
			continue;
		
		printf("[%s:%d] client accept\n", inet_ntoa(clientaddr.sin_addr), ntohs(clientaddr.sin_port));
		if (pthread_create(&tid, NULL, judgeThread, (void *)&clientsock) < 0)
		{
			fprintf(stderr, "pthread_create error, socket close\n");
			close(clientsock);
		}
		pthread_cond_wait(&cond, &mutex);
		pthread_detach(tid);
	}
	pthread_mutex_destroy(&mutex);
	pthread_mutex_destroy(&mutex2);
	pthread_cond_destroy(&cond);
	pthread_cond_destroy(&cond2);
}
void *judgeThread(void *arg)
{
	int clientsock = *(int *)arg;
	char readflag = -1;
	pthread_t tid;

	pthread_cond_signal(&cond);

	if (read(clientsock, &readflag, 1) < 0)
	{
		fprintf(stderr, "read flag error, socket close\n");
		close(clientsock);
		pthread_exit(0);
	}
	switch (readflag)
	{
	case 4:
		if (pthread_create(&tid, NULL, uploadThread, (void *)&clientsock) < 0)
		{
			fprintf(stderr, "pthread_create error, socket close\n");
			close(clientsock);
			pthread_exit(0);
		}
		break;
	case 5:
		if (pthread_create(&tid, NULL, downloadThread, (void *)&clientsock) < 0)
		{
			fprintf(stderr, "pthread_create error, socket close\n");
			close(clientsock);
			pthread_exit(0);
		}
		break;
		default : 
			pthread_exit(0);
	}
	pthread_detach(tid);
	pthread_cond_wait(&cond2, &mutex2);
}

void *uploadThread(void *arg)
{

	int sock = *(int *)arg;
	char msg[MAX_MSG_BUF];
	char *ptr = msg;
	char mediaUrl[MAX_URL];
	char recvmsg[MAX_UPLOAD_BUF];

	pthread_cond_signal(&cond2);
	int nread = 0, msgsize = 74;
	int size, fd;

	time_t now;

	struct media_data data;

	memset(&data, 0, sizeof(data));
	memset(msg, 0, sizeof(msg));

	while (msgsize)
	{
		if ((nread = read(sock, ptr, msgsize)) < 0)
		{
			fprintf(stderr, "downloadThread : read error %d\n", sock);
			break;
		}
		msgsize -= nread;
		ptr += nread;
	}
	ptr = msg;
	strncpy(data.title, ptr, MAX_TITLE);

	ptr += MAX_TITLE;
	strncpy(data.id, ptr, MAX_ID);

	ptr += MAX_ID;
	size = *(int *)ptr;
	data.size = size;

	now = time(NULL);
	mkdir("./upload", FILE_MODE);

	sprintf(mediaUrl, "./upload/%s_%ld", data.title, now);
	if ((fd = open(mediaUrl, O_RDWR | O_CREAT | O_TRUNC, FILE_MODE)) < 0)
	{
		fprintf(stderr, "open error for %s [%d]\n", mediaUrl, errno);
		close(sock);
		pthread_exit(0);
	}

	while (size > 0)
	{
		if ((nread = read(sock, recvmsg, (sizeof(recvmsg) < size) ? sizeof(recvmsg) : size)) < 0)
			continue;
		write(fd, recvmsg, nread);
		size -= nread;
	}
	if (putMediaData(data, mediaUrl) < 0)
	{
		fprintf(stderr, "putMediaUrl error for \n[media url : %s]\n[id : %s]\n[title :%s]\n", mediaUrl, data.id, data.title);
	}
	printf("upload done\n");
	close(sock);
	close(fd);
}
void *downloadThread(void *arg)
{

	int sock = *(int *)arg;
	char msg[MAX_MSG_BUF];
	char *mediaUrl;
	char *ptr = msg;
	char sendmsg[MAX_DOWNLOAD_BUF];
	int nread = 0, msgsize = 4;
	int nwrite = 0, wrtiesize = 0;
	int key, size;
	int fd;
	pthread_cond_signal(&cond2);
	memset(msg, 0, sizeof(msg));

	while (msgsize)
	{
		if ((nread = read(sock, ptr, msgsize)) < 0)
		{
			close(sock);
			fprintf(stderr, "downloadThread : read error\n");
			break;
		}
		msgsize -= nread;
		ptr += nread;
	}

	ptr = msg;
	key = *(int *)ptr;
	mediaUrl = getMediaUrl(ptr);

	if (mediaUrl == NULL)
	{
		fprintf(stderr, "getMediaUrl error\n");
		close(sock);
		pthread_exit(0);
	}

	if ((fd = open(mediaUrl, O_RDWR)) < 0)
	{
		fprintf(stderr, "open error for %s [%d]\n", mediaUrl, errno);
		close(sock);
		pthread_exit(0);
	}

	size = lseek(fd, 0, SEEK_END);
	write(sock, (char *)&size, sizeof(size));

	lseek(fd, 0, SEEK_SET);
	while ((nread = read(fd, sendmsg, sizeof(sendmsg))) > 0)
	{
		write(sock, sendmsg, nread);
	}
	printf("download done\n");
	shutdown(sock, SHUT_WR);
	close(fd);
}

char *getMediaUrl(char *_key)
{
	char *res = malloc(sizeof(char) * 256);
	char query[512];
	int keyindex = *(int *)_key;
	printf("key :%d\n",keyindex);
	memset(query, 0, sizeof(query));
	MYSQL mysql;
	mysql_init(&mysql);
	if (mysql_real_connect(&mysql, NULL, "root", "dhkdzhr3#", "Net", 3306, (char *)NULL, 0) == NULL)
	{
		fprintf(stderr, "mysql connection error\n");
		return NULL;
	}

	sprintf(query, "SELECT _url FROM media where _key = %d", keyindex);

	mysql_query(&mysql, query);
	MYSQL_RES *result = mysql_store_result(&mysql);
	if (result == NULL)
	{
		return NULL;
	}
	int num_fields = mysql_num_fields(result);
	MYSQL_ROW row = mysql_fetch_row(result);

	mysql_free_result(result);
	mysql_close(&mysql);
	memset(res, 0, sizeof(res));
	/*test*/
	strcpy(res, row[0]);
	printf("reas : %s\n",res);
	/*****/
	return res;
}
int putMediaData(struct media_data _data, char *_mediaName)
{
	char query[512];
	memset(query, 0, sizeof(query));
	MYSQL mysql;
	mysql_init(&mysql);
	if (mysql_real_connect(&mysql, NULL, "root", "dhkdzhr3#", "Net", 3306, (char *)NULL, 0) == NULL)
	{
		fprintf(stderr, "mysql connection error\n");
		return -1;
	}

	sprintf(query, "INSERT INTO media (_title, _id, _size, _view, _url)\n VALUES(\'%s\',\'%s\',%d,0,\'%s\')", _data.title, _data.id, _data.size, _mediaName);
	mysql_query(&mysql, query);

	mysql_close(&mysql);
	return 0;
}
void control_signal(int signo)
{
	pid_t ppid = getppid();
	fprintf(stderr, "ppid : %d\n", ppid);
	kill(ppid, SIGUSR1);
}
