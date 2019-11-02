#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>
#include "/usr/include/mysql/mysql.h"



#define Q_MAX_SIZE 512

#define BUF_SIZE 100
#define MAX_CLNT 1000

#define LOGIN_SUCCESS "11"
#define LOGIN_FAIL "12"
#define SIGNUP_SUCCESS "21"
#define SIGNUP_FAIL "22"

typedef struct customer{
   int padding;
   char id[12];
   char passwd[12];
   char name[11];
}CUSTOMER;

typedef struct media_data{
   int size;
   int views;
   char key[5];
   char title[61];
   char id[11];
}MEDIA;

int clnt_cnt=0;
int clnt_socks[MAX_CLNT];
pthread_mutex_t mutx;
void * handle_clnt(void * arg);
void send_msg(char * msg, int len, int sock);
void error_handling(char * msg);
void controller(char* code, int clnt_sock, int pivot);
void send_media_data(int sock, struct media_data * media, int cnt);
int convert(char *msg);
void int_to_hex(int input, char* output);


int SIGN(CUSTOMER c);
int login(CUSTOMER *c);
char *getMediaUrl(int _key);
int searchByID(char *id, MEDIA **m);
int searchByTitle(char *id, MEDIA **m);
int sortByViews(MEDIA **m);

int main(int argc, char *argv[])
{
   int serv_sock, clnt_sock;
   struct sockaddr_in serv_adr, clnt_adr;
   int clnt_adr_sz;
   pthread_t t_id;
   if(argc!=2) {
      printf("Usage : %s <port>\n", argv[0]);
      exit(1);
   }

   pthread_mutex_init(&mutx, NULL);
   serv_sock=socket(PF_INET, SOCK_STREAM, 0);

   memset(&serv_adr, 0, sizeof(serv_adr));
   serv_adr.sin_family=AF_INET; 
   serv_adr.sin_addr.s_addr=htonl(INADDR_ANY);
   serv_adr.sin_port=htons(atoi(argv[1]));

   if(bind(serv_sock, (struct sockaddr*) &serv_adr, sizeof(serv_adr))==-1)
      error_handling("bind() error");
   if(listen(serv_sock, 5)==-1)
      error_handling("listen() error");

   while(1)
   {
      clnt_adr_sz=sizeof(clnt_adr);
      clnt_sock=accept(serv_sock, (struct sockaddr*)&clnt_adr,&clnt_adr_sz);

      pthread_mutex_lock(&mutx);
      clnt_socks[clnt_cnt++]=clnt_sock;
      pthread_mutex_unlock(&mutx);

      pthread_create(&t_id, NULL, handle_clnt, (void*)&clnt_sock);
      pthread_detach(t_id);
      printf("Connected client IP: %s \n", inet_ntoa(clnt_adr.sin_addr));
   }
   close(serv_sock);
   return 0;
}

void * handle_clnt(void * arg)
{
   int clnt_sock=*((int*)arg);
   int str_len=0, i;
   char msg[BUF_SIZE];
    int first_input = 0;
   while((str_len=read(clnt_sock, msg, 1))!=0){
      controller(msg, clnt_sock, first_input);
        first_input = 1;
   }
   printf("connection end");
   fflush(stdout);
   pthread_mutex_lock(&mutx);
   for(i=0; i<clnt_cnt; i++)   // remove disconnected client
   {
      if(clnt_sock==clnt_socks[i])
      {
         while(i++<clnt_cnt-1)
            clnt_socks[i]=clnt_socks[i+1];
         break;
      }
   }
   clnt_cnt--;
   pthread_mutex_unlock(&mutx);
   close(clnt_sock);
   return NULL;
}

void send_msg(char * msg, int len, int sock)   // send to all
{
   pthread_mutex_lock(&mutx);
   write(sock, msg, len);
   write(sock, "\r\n", 2);
   pthread_mutex_unlock(&mutx);
}

void error_handling(char * msg)
{
   fputs(msg, stderr);
   fputc('\n', stderr);
   exit(1);
}

int find_first_zero(char * msg){
   for(int i = 0; 1; i++){
      if(msg[i] != '0')
         return i;   
   }
}

void controller(char* code, int clnt_sock, int pivot){
   char msg[100] = {0};
   struct customer ct = {0};
   struct media_data *md = 0;
   int md_cnt = 0;
      printf("code[0] : %c\n",code[0]);
   switch(code[0]){
      case '1':
         read(clnt_sock, msg, 10);
         strcpy(ct.id, msg+find_first_zero(msg));
         memset(msg, 0, 100);
         read(clnt_sock, msg, 10);
         strcpy(ct.passwd, msg+find_first_zero(msg));
         if(login(&ct))
         {
            memset(msg, 0, 100);
            strcpy(msg, LOGIN_SUCCESS);
            strncat(msg, "00000000", 8-strlen(ct.name));
            strcat(msg, ct.name);
            send_msg(msg, 10, clnt_sock);
         }
         else
         {
             printf("SEND_MSG\n");
            send_msg(LOGIN_FAIL, 2, clnt_sock);
            
             printf("SEND_MSG_END\n");
         }


         break;
      case '2':
         read(clnt_sock, msg, 10);
         strcpy(ct.id, msg+find_first_zero(msg));
         memset(msg, 0, 100);
         read(clnt_sock, msg, 10);
         strcpy(ct.passwd, msg+find_first_zero(msg));
         memset(msg, 0, 100);
         read(clnt_sock, msg, 8);
         strcpy(ct.name, msg+find_first_zero(msg));
         if(SIGN(ct))
         {
            send_msg(SIGNUP_SUCCESS, 2, clnt_sock);
         }
         else
         {
            send_msg(SIGNUP_FAIL, 2, clnt_sock);
         }
         break;
      case '3':
            if(pivot != 0)
                return;
            md_cnt = sortByViews(&md);
            send_media_data(clnt_sock, md, md_cnt);
         
         break;
      case '6':
         read(clnt_sock, msg, 1);
         if(msg[0] == '1'){
            int temp = 0;
            read(clnt_sock, msg, 10);
            for(int i = 0; i<10; i++){
                if(msg[i] != '0'){
                    temp = i;
                    break;
                }
            }
            md_cnt = searchByID(msg+temp, &md);
         }
         else{
            int temp = 0;
            read(clnt_sock, msg, 60);
            for(int i = 0; i<60; i++){
                if(msg[i] != '0'){
                    temp = i;
                    break;
                }
            }
            read(clnt_sock, msg, 60);
            md_cnt = searchByTitle(msg+temp, &md);
         }

         send_media_data(clnt_sock, md, md_cnt);
         break;
   }
}


void send_media_data(int sock, struct media_data * media, int cnt){
   char msg[90] = {0};
   char *data = (char*)malloc(sizeof(char)*8);
   strcpy(msg, "31");
    int_to_hex(cnt, data);
    strcat(msg, data);
   send_msg(msg, 10, sock);
   for(int i = 0; i<cnt; i++){
      strcpy(msg, "32");
      strcat(msg, media[i].key);
      strncat(msg,"000000000000000000000000000000000000000000000000000000000000", 60-strlen(media[i].title));
      strcat(msg, media[i].title);
      strncat(msg, "0000000000", 10-strlen(media[i].id));
      strcat(msg, media[i].id);
      int_to_hex(media[i].size, data);
      strcat(msg, data);
      int_to_hex(media[i].views, data);
      strcat(msg, data);
    printf("%s\n", msg);
      strcat(msg+2, (char*)&media[i]);
      send_msg(msg, 88, sock);
   }
}

int convert(char *input){
   int ret = 0;
   int multi = 1;
   char msg;
   for(int i = 3; i>=0; i--){
      msg = input[i];
      if(msg >= '0' && msg <= '9')
         ret+=(msg - '0') *multi;
      else
         ret += (msg -'A' + 10)*multi;
      multi*=16;
   }
   return ret;
}

void int_to_hex(int input, char* output){
   sprintf(output, "%08x", input);
   for(int i = 0; i<4; i++)
      if(output[i] >= 'a' && output[i] <='z')
         output[i] = 'A'+(output[i]-'a');
}

int SIGN(CUSTOMER c)
{
    MYSQL mysql;
    MYSQL_RES *res;
    MYSQL_ROW row;
    int field;
    char rowCheckQuery[Q_MAX_SIZE] = "SELECT * FROM user";
    char query[Q_MAX_SIZE];
    sprintf(query,"INSERT IGNORE INTO user  VALUES(\'%s\',\'%s\',\'%s\')",c.id,c.passwd,c.name);
    int rowsize;
    int checkSize = strlen(rowCheckQuery);
    int length = strlen(query);

    mysql_init(&mysql);
    if (!mysql_real_connect(&mysql, NULL, "root", "malloc","Net", 3306, (char *)NULL, 0))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }

    if (mysql_query(&mysql, "USE Net"))
    {
        printf("%s\n",mysql_error(&mysql));
        exit(1);
    }

    if (mysql_real_query(&mysql, rowCheckQuery, checkSize))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }
    res = mysql_store_result(&mysql);
    rowsize = mysql_num_rows(res);
    int tmp = rowsize;

    mysql_free_result(res);

    if (mysql_real_query(&mysql, query, length))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }
    if (mysql_real_query(&mysql, rowCheckQuery, checkSize))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }

    res = mysql_store_result(&mysql);
    rowsize = mysql_num_rows(res);

    mysql_close(&mysql);
    if (tmp == rowsize)
    {
        return 0; // 회원가입 실패
    }
    return 1; // 회원가입 성공
}


int login(CUSTOMER *c)
{
    MYSQL mysql;
    MYSQL_RES * res;
    MYSQL_ROW row;
    char query[Q_MAX_SIZE];// = "SELECT * FROM user WHERE _id='c.id' AND pw='c.pw'";
    sprintf(query,"SELECT * FROM user WHERE _id=\'%s\' AND _pw=\'%s\'",c->id,c->passwd);
    int length = strlen(query);

    mysql_init(&mysql);
    if (!mysql_real_connect(&mysql, NULL, "root", "dhkdzhr3#","Net", 3306, (char *)NULL, 0))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }

    if (mysql_query(&mysql, "USE Net"))
    {
        printf("%s\n",mysql_error(&mysql));
        exit(1);
    }

    if (mysql_real_query(&mysql, query, length))
    {
        printf("%s\n",mysql_error(&mysql));
        exit(1);
    }
    res = mysql_store_result(&mysql);
    if(res == NULL){
        printf("rea == NULL\n");
        return 0;
    }
    row = mysql_fetch_row(res);
    if(row == NULL){
        printf("row == NULL\n");
        return 0;
    }
    if (mysql_num_rows(res) == 0)
    {
        return 0; // 로그인 실패
    }
    else
    {

       strncpy(c->name,row[2], 8);
        //printf("%s\n",row[3]);
        return 1; // 로그인 성공
    }
 
    mysql_close(&mysql);


}


int searchByID(char *id, MEDIA **m)
{ // id 받고 m 전달
    MYSQL mysql;
    MYSQL_ROW row;
    MYSQL_RES *res;

    char query[Q_MAX_SIZE];// = "SELECT * FROM media WHERE _id LIKE '%id%'";
    sprintf(query,"SELECT * FROM media WHERE _id LIKE \'%%%s%%\'",id);
    printf("%s\n",query);
    int cnt;
    int length = strlen(query);
    int field;
    int j = 0;
    MEDIA * md;
    mysql_init(&mysql);

    if (!mysql_real_connect(&mysql, NULL, "root", "dhkdzhr3#","Net", 3306, (char *)NULL, 0))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }
    if(mysql_query(&mysql, "USE Net")){
        printf("%s\n",mysql_error(&mysql));
        exit(1);
    }
    if(mysql_real_query(&mysql, query, length)){
        exit(1);
    }
    res = mysql_store_result(&mysql);
    cnt = mysql_num_rows(res);
    md = (MEDIA*)malloc(sizeof(MEDIA)*cnt);
    field = 6;
    while( (row = mysql_fetch_row(res))){

            strcpy(md[j].key,row[0]);
            strcpy(md[j].title,row[1]);
            strcpy(md[j].id,row[2]);
            md[j].size=atoi(row[3]);
            md[j].views=atoi(row[4]);

            for(int k = 0 ; k < 5 ; k++){
                printf("%s\n", row[k]);
            }
        j++;
    }
    *m = md;
    mysql_free_result(res);
    mysql_close(&mysql);
    return cnt;
}


int sortByViews(MEDIA **m){
    MYSQL mysql;
    MYSQL_ROW row;
    MYSQL_RES *res;

    char query[Q_MAX_SIZE];
    sprintf(query,"SELECT * FROM media ORDER BY _view DESC LIMIT 10");
    int cnt;
    int length = strlen(query);
    int field;
    int j = 0;
    MEDIA * md;
    mysql_init(&mysql);

    if (!mysql_real_connect(&mysql, NULL, "root", "dhkdzhr3#","Net", 3306, (char *)NULL, 0))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }
    if(mysql_query(&mysql, "USE Net")){
        printf("%s\n",mysql_error(&mysql));
        exit(1);
    }
    if(mysql_real_query(&mysql, query, length)){
        exit(1);
    }
    res = mysql_store_result(&mysql);
    cnt = mysql_num_rows(res);
    md = (MEDIA*)malloc(sizeof(MEDIA)*cnt);
    field = 6;
    while( (row = mysql_fetch_row(res))){

            strcpy(md[j].key,row[0]);
            strcpy(md[j].title,row[1]);
            strcpy(md[j].id,row[2]);
            md[j].size=atoi(row[3]);
            md[j].views=atoi(row[4]);

            for(int k = 0 ; k < 5 ; k++){
                printf("%s\n", row[k]);
            }
        j++;
    }
    *m = md;
    mysql_free_result(res);
    mysql_close(&mysql);
    return cnt;
}
int searchByTitle(char *title, MEDIA **m)
{ // title 받고 m 전달
    MYSQL mysql;
    MYSQL_ROW row;
    MYSQL_RES *res;
    MEDIA * md;
    char query[Q_MAX_SIZE];// = "SELECT * FROM media WHERE _title LIKE '%id%'";
    sprintf(query,"SELECT * FROM media WHERE _title LIKE \'%%%s%%\'",title);
    printf("%s\n",query);
    int length = strlen(query);
    int field, cnt;
    int j = 0;
    
    mysql_init(&mysql);

    if (!mysql_real_connect(&mysql, NULL, "root", "dhkdzhr3#","Net", 3306, (char *)NULL, 0))
    {
        printf("%s\n", mysql_error(&mysql));
        exit(1);
    }
    if(mysql_query(&mysql, "USE Net")){
        printf("%s\n",mysql_error(&mysql));
        exit(1);
    }
    if(mysql_real_query(&mysql, query, length)){
        exit(1);
    }
    res = mysql_store_result(&mysql);
    cnt = mysql_num_rows(res);
    md = (MEDIA*)malloc(sizeof(MEDIA)*cnt);
    field = 6;
    while( row = mysql_fetch_row(res)){
            strcpy(md[j].key,row[0]);
            strcpy(md[j].title,row[1]);
            strcpy(md[j].id,row[2]);
            md[j].size=atoi(row[3]);
            md[j].views=atoi(row[4]);

            for(int k = 0 ; k < 5 ; k++){
                printf("%s\n", row[k]);
            }
        j++;
    }
    *m = md;
    mysql_free_result(res);
    mysql_close(&mysql);
    return cnt;
}