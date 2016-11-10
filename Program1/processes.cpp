#include <iostream>   // for i/o
#include <unistd.h>   // for fork, pipe
#include <stdlib.h>   // for exit
#include <sys/wait.h> // for wait
#include <stdio.h>    // for perror
using namespace std;

int main(int argc, char* argv[])
{
	// input check   
	if (argc != 2)
    {
        perror("Usage: processes <process name>");
        exit(EXIT_FAILURE);
    } 
        
	// local fields    
	enum {READ, WRITE};
    pid_t pid1, pid2, pid3;    
    
    // start forking processes	
	if ((pid1 = fork()) < 0) 
	{	
		perror("fork error");
		exit(EXIT_FAILURE);
	}
	else if (pid1 == 0) 
    {
        // set up second pipe (pipes are used in reverse order)
        int pipeFD2[2];
        if (pipe(pipeFD2) < 0)
        {
            perror("pipe error");
            exit(EXIT_FAILURE);
        }
        
        if ((pid2 = fork()) < 0)
        {	
            perror("fork error");
            exit(EXIT_FAILURE);
        }
        else if (pid2 == 0) 
        {
            // set up first pipe
            int pipeFD1[2];
            if (pipe(pipeFD1) < 0)
            {
                perror("pipe error");
                exit(EXIT_FAILURE);
            }
            
            if ((pid3 = fork()) < 0)
            {	
                perror("fork error");
                exit(EXIT_FAILURE);
            }	
            else if (pid3 == 0) //great grand child
            {              
                close(pipeFD1[READ]);
                dup2(pipeFD1[WRITE], 1);
                close(pipeFD2[READ]);
                close(pipeFD2[WRITE]);
                execlp("ps", "ps", "-A", NULL);               
            }
            else //grandchild
            {
                dup2(pipeFD1[READ], 0); 
                close(pipeFD1[WRITE]);               
                close(pipeFD2[READ]);
		        dup2(pipeFD2[WRITE], 1);
                wait(NULL);
                execlp("grep", "grep", argv[1], NULL);                	
            }
        }
        else //child
        {
            dup2(pipeFD2[READ], 0);
            close(pipeFD2[WRITE]);
            wait(NULL); 
            execlp("wc", "wc", "-l", NULL);
        }		
    }
	else //parent
	{
		wait(NULL);
	}
    
	// goodbye    
	exit(EXIT_SUCCESS); 
}
