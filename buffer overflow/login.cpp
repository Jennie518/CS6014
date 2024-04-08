//
// Created by 詹怡君 on 4/5/24.
//

#ifndef BUFFER_OVERFLOW_LOGIN_C
#define BUFFER_OVERFLOW_LOGIN_C

/* login.c for u1475897 */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>

int check_secret(const char *password, int pwLen) {
    if (pwLen == -1) {
        printf("problem reading password.txt\n");
        return 0;
    } else {
        const char *pw = "superSecretPassword";
        return (pwLen == strlen(pw)) && (memcmp(password, pw, pwLen) == 0);
    }
}

int check_secret1(const char *password, int pwLen) {
    return check_secret(password, pwLen);
}

int check_secret2(const char *password, int pwLen) {
    return check_secret1(password, pwLen);
}


extern char** environ;
static char * sh = "/bin/sh";
void success(){
    char  * argv[2] = {sh, NULL};
    puts("successful login!\n");
    execve(sh, argv, environ);
}

void failure(){
    puts("wrong password\n");
}


int login(){
    char password[24];
    int fd = open("password.txt", O_RDONLY);
    printf("enter your password:\n");
    int pwLen = read(fd, password, 1000); // just read the whole file...
    close(fd);
    return check_secret2(password, pwLen);
}


int main(){
    int res = login();
    if (res) {
        success();
    } else {
        failure();
    }

    puts("exiting in main\n");
    return 0;
}
#endif //BUFFER_OVERFLOW_LOGIN_C
