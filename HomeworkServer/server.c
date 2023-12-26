#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <signal.h>
#include <libpq-fe.h>

void sigint_handler(int signum);  // обработка сигнала Ctrl+C

int PORT = 45125;
char *welcome = "Welcome to the server\n";
#define BUFLEN 4096
#define MAX_CLIENTS 6
const static char conninfo[] = "hostaddr=82.179.140.18 port=5432 dbname=homework user=mpi password=135a1";
const char* REGISTER_SUCCESS = "Пользователь успешно зарегистрирован.";
const char* REGISTER_FAIL = "Не удалось зарегистрировать пользователя.";
const char* HELP_MESSAGE = "/quit - выйти с сервера;\n/register - зарегистрировать новый аккаунт;\n/login - войти в существующий аккаунт;\n";
const char* QUIT_MESSAGE = "Goodbye.";
const char* key = "OBOBUS137";
const char* blockerator = "You shall not pass!";
int sockfd;  // дескриптор сокета 
PGconn* conn; // объект подключения

// Обработка сигнала Ctrl+C
void sigint_handler(int signum)
{
    close(sockfd);
    PQfinish(conn);
    exit(0);
}

enum RequestType
{
    GET,
    POST,
};

enum ResponseType
{
    SUCCESS,
    FAIL,
};

struct Request
{
    enum RequestType type;
    char* command;
    char* request_string; 
};

struct Response
{
    enum ResponseType type;
    char* response_message;
};

int checkMultipleObjects(char* string){
    int numLines = 0;
    for (const char *c = string; *c != '\0'; ++c){
        if (*c == '\n'){
            numLines++;
            if(numLines > 1){
                return 1;
            }
        }
    }
    return 0;
}


//prop_name:prop_value;prop_name:prop_value
void serialize(char* string, char* object_name, char* serialized)
{
    printf("\n-----------------------\nBEFORE SERIALIZATION\n\"%s\" with object_name %s\n",string, object_name);
    if(strcmp(string," ")==0){
        return;
    }
    char* row_string = malloc(sizeof(char)*256);
    char *row,*token, *rest;
    char *prop_name, *prop_value;
    char *tmp, *rowtmp;


    rest = strdup(string);

    int multipleRows = checkMultipleObjects(rest);

    if(multipleRows == 1){
        strcat(serialized,"[{");
    }
    else{
        strcat(serialized,"{");
    }
    while((row=strsep(&rest,"\n"))!=NULL){
        rowtmp = strdup(row);
        while( (token=strsep(&rowtmp,";")) != NULL)
        {
            tmp = strdup(token);
            prop_name = strtok(tmp,":");
            prop_value = strtok(NULL,":");
            if(prop_name == NULL || prop_value == NULL){
                break;
            }
            sprintf(row_string, "\"%s\":\"%s\",",prop_name,prop_value);
            strcat(serialized,row_string);
        }
        serialized[strlen(serialized)-1]=' ';
        if(multipleRows == 1 && strcmp(row,"") != 0){
            strcat(serialized,"}, {");
        }
    }
    if(multipleRows == 1){
        serialized[strlen(serialized) - 3] = ']';
        serialized[strlen(serialized) - 2] = ' ';
        serialized[strlen(serialized) - 1] = ' ';
    }
    else{
        strcat(serialized,"}");
    }

    printf("\n-----------------------\nAFTER SERIALIZATION\n%s",serialized);
    return;
}
///POST|homework@{"subject_id":"1","homework_title":"Матеша","homework_description":"Решить 100 дифуров"}
void deserialize(char* string,char* deserialized)
{    
    printf("\n-----------------------\nBEFORE DESERIALIZATION\n%s",string);
    char *token, *row;
    char *prop_value;
    char *rest;
    char *tmp, *tmptok;

    char* serialized = strdup(string);
    row = strtok(serialized,":");
    row = strtok(NULL,":");
    rest = strdup(row);
    while( (token=strsep(&rest,",")) != NULL)
    {
        tmp = strdup(token);
        tmptok = strsep(&tmp,"\"");
        if(tmptok == NULL){
            prop_value = token;
        }
        else{
            while((tmptok = strsep(&tmp,"\"")) != NULL){
                if(strcmp(tmptok,"") != 0 && strcmp(tmptok," ") != 0 && strcmp(tmptok,"}") != 0){
                    prop_value = tmptok;
                }
            }   
        }
        strcat(deserialized,prop_value);
        strcat(deserialized,",");
        rest = strtok(NULL,":");
        if(rest == NULL){
            break;
        }
    }

    printf("\n-----------------------\nAFTER DESERIALIZATION\n%s",deserialized);
    return;
}

char* serialize_response(char* response_type, char* response_message){
    char serialized[BUFLEN]="";
    strcat(serialized, "{");
    strcat(serialized, "\"type\":\"");
    strcat(serialized, response_type);
    strcat(serialized,"\",\"message\":\"");
    strcat(serialized, response_message);
    strcat(serialized,"\"}\n");
    printf("serialized: %s\n",serialized);
    char* res = serialized;
    return res;
}

// Выполнение sql-запроса
void executeSQL(PGconn* conn, char* request, const char **param_values, int num_params, char* result_string)
{
    //TODO: Форматирование данных: 
    //column_name:value;column_name:value;
    printf("\n-----------------------\n SQL to execute\n Request:%s\n",request);
    char* result = (char*)malloc(BUFLEN);
    memset(result, 0, BUFLEN);

    // Начать транзакцию
    PGresult* res = PQexec(conn, "BEGIN TRANSACTION");
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        fprintf(stderr, "BEGIN command failed: %s", PQerrorMessage(conn));
        exit(1);
    }
    printf("BEGAN\n");

    // Очистить res, чтобы избежать утечки памяти
    PQclear(res);
    // Выполнить запрос
    res = PQexecParams(conn, request, num_params, NULL, param_values, NULL, NULL, 0);

    if (PQresultStatus(res) == PGRES_COMMAND_OK)
    {
        int affected_rows = atoi(PQcmdTuples(res));
        snprintf(result, BUFLEN, "%d", affected_rows);
    }
    else if (PQresultStatus(res) == PGRES_TUPLES_OK)
    {
        for (int i = 0; i < PQntuples(res); i++)
        {
            for (int j = 0; j < PQnfields(res); j++)
            {
                strcat(result,PQfname(res,j));
                strcat(result,":");
                strcat(result, PQgetvalue(res, i, j));
                strcat(result, ";");
            }
            strcat(result, "\n");
        }
        if (strlen(result) > 0){
            strcat(result, "\n");
            result[strlen(result) - 1] = '\0';
        }
        if (PQntuples(res) == 0)
            result[0] = ' ';
    }
    else
    {
        fprintf(stderr, "Sql command failed: %s", PQerrorMessage(conn));
        snprintf(result, BUFLEN, "%d", -1);
    }
    printf("%s\n",result);
    // Очистить res
    PQclear(res);

    // Завершить транзакцию
    res = PQexec(conn, "COMMIT");
    PQclear(res);

    strcpy(result_string,result);
}

void processGET(PGconn* conn, char* request_string, char* result){
    char sql_result [BUFLEN];

    char* dup = strdup(request_string);
    char* entity = strtok(dup,"@");
    char* par1 = strtok(NULL,"@");
    const char* parameter[] = {par1};
    parameter[0] = par1;
    printf("entity:\"%s\"\n",entity);

    if(strcmp(entity,"schedule")==0){
        printf("\nGET schedule request\n%s\n",parameter[0]);
        executeSQL(conn, "SELECT * FROM subject WHERE user_id = $1 ORDER BY subject_week_position, subject_day_position ASC", parameter, 1,sql_result);
        strcat(result,"schedule+");
        strcat(result,sql_result);
        return;
    }
    else if(strcmp(entity,"homework")==0){
        printf("\nGET homework request\n%s\n",parameter[0]);
        executeSQL(conn, "SELECT * FROM homework WHERE user_id = $1", parameter, 1,sql_result);
        strcat(result,"homework+");
        strcat(result,sql_result);
        return;
    }
    else{
        printf("NOT FOUND GET\n");
        strcpy(result,"-1");
        return;
    }
    
}

void processPOST(PGconn* conn,char* request_string, char* result){
    char sql_result [BUFLEN];
    char* dup = strdup(request_string);
    char* entity = strtok(dup,"@"); 
    printf("entity:\"%s\"\n",entity);

    if(strcmp(entity,"login")==0){
        char* par1 = strtok(NULL,"@");
        char* par2 = strtok(NULL,"@");
        const char* parameter[] = {par1,par2};
        parameter[0] = par1;
        parameter[1] = par2;
        executeSQL(conn, "SELECT * FROM users WHERE login =$1 AND password = $2", parameter, 2,sql_result);
        printf("login: \"%s\" password: \"%s\" execSQLresult: \"%s\"\n",parameter[0],parameter[1],sql_result);
        if(strlen(sql_result) == 1){
            strcpy(result,"-2");
            return;
        }
        strcat(result,"user+");
        strcat(result,sql_result);
        return;
    }
    else if(strcmp(entity,"register")==0){
        char* par1 = strtok(NULL,"@");
        char* par2 = strtok(NULL,"@");
        const char* parameter[] = {par1,par2};
        parameter[0] = par1;
        parameter[1] = par2;
        executeSQL(conn, "SELECT * FROM users WHERE login =$1 AND password = $2", parameter, 2,sql_result);
        printf("login: \"%s\" password: \"%s\" execSQLresult: \"%s\"\n",parameter[0],parameter[1],sql_result);
        if(strlen(sql_result) != 1){
            strcpy(result,"-3");
            return;
        }
        executeSQL(conn,"INSERT INTO users (login, password) VALUES ($1,$2)",parameter,2,sql_result);
        executeSQL(conn, "SELECT * FROM users WHERE login =$1 AND password = $2", parameter, 2,sql_result);

        printf("execSQLresult: \"%s\"",sql_result);

        strcat(result,"user+");
        strcat(result,sql_result);

        return;     
    }
    else if(strcmp(entity,"homework")==0){
        char deserialized[BUFLEN] = "";
        deserialize(strtok(NULL,"@"),deserialized);
        char* tmp = strdup(deserialized);
        char* param1 = strtok(tmp, ",");
        char* param2 = strtok(NULL,",");
        char* param3 = strtok(NULL,",");

        const char* parameter[] = { param1, param2, param3};
        parameter[0] = param1;
        parameter[1] = param2;
        parameter[2] = param3;

        executeSQL(conn, "INSERT INTO homework (user_id, homework_title, homework_description) VALUES ($1,$2,$3)", parameter, 3,sql_result);
        return;
    }
    else if(strcmp(entity,"schedule")==0){
        char deserialized[BUFLEN] = "";
        deserialize(strtok(NULL,"@"),deserialized);
        char* tmp = strdup(deserialized);
        char* param1 = strtok(tmp, ",");
        char* param2 = strtok(NULL,",");
        char* param3 = strtok(NULL,",");
        char* param4 = strtok(NULL,",");

        const char* parameter[] = { param1, param2, param3, param4};
        parameter[0] = param1;
        parameter[1] = param2;
        parameter[2] = param3;
        parameter[3] = param4;

        executeSQL(conn, "SELECT * FROM subject WHERE subject_day_position = $2 AND subject_week_position = $3 AND user_id = $4", parameter, 4,sql_result);
        if(strlen(sql_result) > 0){
            executeSQL(conn, "UPDATE subject SET subject_title = $1 WHERE subject_day_position = $2 AND subject_week_position = $3 AND user_id = $4", parameter, 4,sql_result);
        }
        else{
            executeSQL(conn, "INSERT INTO subject (subject_title, subject_day_position, subject_week_position,user_id) VALUES ($1,$2,$3,$4)", parameter, 4,sql_result);
        }
        return;
    }
    
    strcpy(result,"-1");
    return;    

}

void processDELETE(PGconn* conn,char* request_string, char* result){

    char sql_result [BUFLEN];

    char* dup = strdup(request_string);
    char* entity = strtok(dup,"@");
    char* par1 = strtok(NULL,"@");
    const char* parameter[] = {par1};
    parameter[0] = par1;
    printf("entity:\"%s\"\n",entity);

    if(strcmp(entity,"homework")==0){
        executeSQL(conn, "SELECT * FROM homework WHERE homework_id = $1", parameter, 1,sql_result);
        if(strlen(sql_result) > 0){
            executeSQL(conn, "DELETE homework WHERE homework_id = $1", parameter, 1,sql_result);
        }
        else{
            strcpy(result,"-2");
        }
        return;
    }
    else if(strcmp(entity,"schedule")==0){
        executeSQL(conn, "SELECT * FROM subject WHERE subject_id = $1", parameter, 1,sql_result);
        if(strlen(sql_result) > 0){
            executeSQL(conn, "DELETE subject WHERE subject = $1", parameter, 1,sql_result);
        }
        else{
            strcpy(result,"-2");
        }
        return;
    }
    
    strcpy(result,"-1");
    return;    

}

int processResponse(int fd, enum ResponseType response_type, char* response_message){
    char* serialize_result;
    int sendRes;
    switch(response_type)
    {
        case SUCCESS:
            printf("SUCCESS\n");
            serialize_result = serialize_response("SUCCESS",response_message);
            break;
        case FAIL:
            printf("FAIL\n");
            serialize_result = serialize_response("FAIL",response_message);
            break;
        default:
            printf("Bad request\n");
            serialize_result = serialize_response("FAIL","Bad request");
            break;
    }
    sendRes = send(fd, serialize_result, BUFLEN, 0);
    if(sendRes<0){
        printf("Send response failed.\n");
        return 0;
    }
    printf("Response sent.\n");
    return 0;
}

struct Response* processRequest(PGconn* conn,int fd,char* command, char* request_string){
    
    char result[BUFLEN] = "";
    enum ResponseType type;
    type = SUCCESS;
    struct Response response;
    char message[BUFLEN] = "";
    response.type = type;
    response.response_message = message;
    struct Response* response_p = &response;


    if(strcmp(command,"/help")==0){
        printf("\nhelp\n");
        type = SUCCESS;
        response_p->type = type;
        strcpy(response_p->response_message, HELP_MESSAGE);
    }

    else if(strcmp(command,"/quit")==0){
        printf("\nquit\n");
        type = SUCCESS;
        response_p->type = type;
        strcpy(response_p->response_message, QUIT_MESSAGE);
    }
    else if(strcmp(command,"/GET")==0){
        printf("\nGET\n");
        processGET(conn,request_string, result);
        printf("GET process successful\n");
        if(strcmp(result,"-1") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "Bad request.";
            return response_p;
        }
        else if(strcmp(result,"-4") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "User is not logged in.Try /POST|login";
            return response_p;
        }

        char* object_name = strtok(result,"+");
        char* executeSQLresult = strtok(NULL,"+");
        type = SUCCESS;
        response_p->type = type;

        if(object_name != NULL && strcmp(executeSQLresult," ") != 0){
            char serialized[BUFLEN] = "";
            serialize(executeSQLresult,object_name,serialized);
            strcpy(response_p->response_message,serialized);
        }
        else{
            type = FAIL;
            response_p->type = type;
            strcpy(response_p->response_message,"Not found");
        }
    }
    else if(strcmp(command,"/POST")==0){
        printf("\nPOST\n");
        processPOST(conn,request_string,result);
        printf("POST process successful\n");

        if(strcmp(result,"-1") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "Bad request.";
            return response_p;
        }
        else if(strcmp(result,"-2") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "Incorrect login or password.";
            return response_p;
        }
        else if(strcmp(result,"-3") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "User already exists.";
            return response_p;
        }
        else if(strcmp(result,"-4") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "User is not logged in.Try /POST|login";
            return response_p;
        }

        char* object_name = strtok(result,"+");
        char* executeSQLresult = strtok(NULL,"+");
        type = SUCCESS;
        response_p->type = type;
        if(object_name != NULL && executeSQLresult != NULL){
            char serialized[BUFLEN] = "";
            serialize(executeSQLresult,object_name,serialized);
            strcpy(response_p->response_message,serialized);
        }
        else{
            strcpy(response_p->response_message,"Item added successfully.");
        }
    }
    else if(strcmp(command,"/DELETE")==0){
        printf("\nPOST\n");
        processPOST(conn,request_string,result);
        printf("POST process successful\n");

        if(strcmp(result,"-1") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "Bad request.";
            return response_p;
        }
        else if(strcmp(result,"-2") == 0)
        {
            type = FAIL;
            response_p->type = type;
            response_p->response_message = "Not found boject to delete.";
            return response_p;
        }
        else{

            type = SUCCESS;
            response_p->type = type;
            strcpy(response_p->response_message,"Item deleted successfully.");
            return response_p;
        }
    }
    else
    {
        printf("BadRequest\n");
        type = FAIL;
        response_p->type = type;
        response_p->response_message = "Bad request.";
    }
    return response_p;
}

int processSession(int fd)
{
    char recvbuf[BUFLEN];
    int blockeratorLength = strlen(blockerator); 
    int res = recv(fd, recvbuf, BUFLEN, 0);
        
    if (res > 0)
    {
        recvbuf[res] = '\0';
        if(strcmp(recvbuf,key)!=0){
            send(fd, blockerator, blockeratorLength, 0);
            close(fd);
            return 0;
        }
    }
    else{
        send(fd, blockerator, blockeratorLength, 0);
        close(fd);
        return 0;
    }


    int welcomeLength = strlen(welcome); 
    send(fd, welcome, welcomeLength, 0);

    conn = 0;
    if (conn == 0) {
        conn = PQconnectdb(conninfo);
        if (PQstatus(conn) != CONNECTION_OK) {
            fprintf(stderr, "Connection to database failed: %s\n", PQerrorMessage(conn));
            PQfinish(conn);
            return 0;
        }
        fprintf(stderr, "Connection to db\n");
    }

    printf("fd=%i\n",fd);
     
    int main_run = 1;
    while (main_run)
    {
        int res = recv(fd, recvbuf, BUFLEN, 0);
        
        if (res > 0)
        {
            recvbuf[res] = '\0';
            printf("Received (%d): %s\n", res, recvbuf);
            char* command = strtok(recvbuf,"|");
            printf("\n----------------\nCommand: %s\n",command);

            char* request_string = strtok(NULL,"|");

            printf("Request: %s\n",request_string);
            struct Response* response = processRequest(conn, fd, command, request_string);

            if(strcmp(response->response_message, "Goodbye.") == 0){
                processResponse(fd,response->type,response->response_message);
                printf("disconected\n");
                close(fd);
                PQfinish(conn);
                break;
            }
            processResponse(fd,response->type,response->response_message);
        }
        else
        {
            printf("disconected\n");
            close(fd);
            PQfinish(conn);
            break;
        }
    }
    return 0;
}

int main() 
{
    struct sockaddr_in addr;

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    //addr.sin_addr.s_addr = inet_addr(ADDRESS);
    addr.sin_port = htons(PORT);
    addr.sin_addr.s_addr = INADDR_ANY;

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) 
    {
        fprintf(stderr, "Unable to create socket\n");
        return 1;
    }

    if (bind(sockfd, (struct sockaddr *) &addr, sizeof(addr)) < 0) 
    {
        fprintf(stderr, "Unable to bind socket\n");
        close(sockfd);
        return 1;
    }

    if (listen(sockfd, 6) < 0) 
    {
        fprintf(stderr, "Unable to listen socket\n");
        close(sockfd);
        return 1;
    }

    fprintf(stdout, "Server started. IP:port - 82.179.140.18:%i. Waiting client connection...\n", PORT);    

    signal(SIGINT, sigint_handler);    

    while (1) 
    {
        struct sockaddr *ca = NULL;
        socklen_t sz = 0;
        int fd = accept(sockfd, ca, &sz);

        if (fd < 0) 
        {
            fprintf(stderr, "Unable to listen socket\n");
            sleep(2);
            continue;
        }

        pid_t pid = fork();        

        if (pid < 0) 
        {
            fprintf(stderr, "Unable to fork process\n");
            close(fd);
            return -1;
        }

        if (pid == 0) 
        {
            fprintf(stderr, "New session started\n");
            close (sockfd);
            processSession(fd);   
            printf("Goodbye\n");         
            return 0;
        }
    }
    printf("Goodbye\n");
    return 0;
}