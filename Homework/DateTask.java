import java.net.*;
import java.io.*;

public class DateTask implements Runnable
{
    Socket client;
    
    public DateTask(Socket client)
    {
       this.client = client;
    }

    public void run()
    {
        try
        {
            PrintWriter pout = new
            PrintWriter(client.getOutputStream(), true);
            // write the Date to the socket
            pout.println(new java.util.Date().toString());
            // close the socket and resume
            // listening for connections
            client.close();
           
        }
        catch (IOException ioe) 
        {
            System.err.println(ioe);
        }    
    }
}