import java.net.*;
import java.io.*;

public class DateThreads
{
    public static void main(String args[])
    {
        try 
        {
            ServerSocket sock = new ServerSocket(6013);
            // now listen for connections
            while (true) 
            {
                Socket client = sock.accept();                
                new Thread(new DateTask(client)).start();                            
            }
        }
        catch (IOException ioe) 
        {
            System.err.println(ioe);
        }        
    }
}