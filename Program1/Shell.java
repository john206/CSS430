public class Shell extends Thread
{    
    // used for prompt
    private int count = 1;
    
    public Shell(){}
    public Shell(String[] args){}
    
    public void run()
    {
        // main command loop
        while (true)
        {
           String[] args = readCmdLine();
           if (args.length == 0)
           {
               count--;
               continue;
           }
           else if(args[0].equals("exit"))
           {
               break;             
           }
           else
           {
               processCmdLine(args);
           }           
        }
        SysLib.cout("goodbye\n");
        SysLib.sync();
        SysLib.exit();        
    }
    
    // read a command + argument set
    private String[] readCmdLine()
    {
        SysLib.cout("Shell[" + count++ + "]% ");
        StringBuffer sb = new StringBuffer();
        SysLib.cin(sb);
        return SysLib.stringToArgs(sb.toString());
    }
    
    // process the command + argument set
    private void processCmdLine(String[] args)
    {
        int length = args.length;
        int tid = -1;
        String cmdStr = "";
        
        for (int i = 0; i < length - 1; i++)
        {
            if (!args[i].equals(";") && !args[i].equals("&")) // arg is a command
            {
                cmdStr += args[i];
                
                if (args[i + 1].equals("&")) // run the command concurrently
                {
                    if (SysLib.exec(SysLib.stringToArgs(cmdStr)) < 0) // something exploded
                    {
                        return;
                    }
                    cmdStr = "";                  
                }
                else if (args[i + 1].equals(";")) // run the command sequentially
                {
                    tid = SysLib.exec(SysLib.stringToArgs(cmdStr));
                    while (tid != -1 && SysLib.join() != tid);
                    cmdStr = "";                  
                }
                else // there are more args in the command
                {
                    cmdStr += " ";
                }                
            }
        }
        
        if (!args[length - 1].equals(";") && !args[length - 1].equals("&")) // last arg is sequential command
        {
            cmdStr += args[length - 1];
            tid = SysLib.exec(SysLib.stringToArgs(cmdStr));
            while (tid != -1 && SysLib.join() != tid);
        }
        
        SysLib.cout("\n");
    }
}