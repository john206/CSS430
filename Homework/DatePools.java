import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class DatePools
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
                DateTask dt = new DateTask(client);
                
                ExecutorService threadExecutor = Executors.newCachedThreadPool();
                threadExecutor.execute(dt);
            }
        }
        catch (IOException ioe) 
        {
            System.err.println(ioe);
        }        
    }
}

