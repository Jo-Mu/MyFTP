import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Myftp 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {  
       if(args.length == 0)
       {
           System.out.println("Error: Please give a server name or IP address");
           System.out.println("Usage: myftp [server-name]");
           System.exit(1);
       }
       
       final String IP = args[0];
       final int PORT = 21;
       MyFTPClient client = null;
       
       try
       {
           client = new MyFTPClient(IP, PORT);
       }
       catch(UnknownHostException exception)
       {
           System.out.println(exception);
           System.exit(1);
       }
       catch(IOException exception)
       {
           System.out.println(exception);
           System.exit(1);
       }
       
       System.out.println("Server connected successfully");
       
       Scanner userInput = new Scanner(System.in);
       boolean successfulLogin = false;
       
       while(!successfulLogin)
       {
           try
           {
               System.out.print("User: ");
               String user = userInput.nextLine();
               System.out.print("Pass: ");
               String pass = userInput.nextLine();
               successfulLogin = client.Login(user, pass);
           }
           catch(IOException exception)
           {
               System.out.println(exception);
               System.exit(1);
           }
           
           if(successfulLogin)
           {
               System.out.println("Login successful");
           }
           else
           {
               System.out.println("Login failed.");
           }
       }
       
       boolean sendingCommands = true;
       
       while(sendingCommands)
       {
           String inputCommand = GetUserCommand(userInput);
           sendingCommands = SelectCommand(inputCommand, client);
       }
    }
    
    //Gets input from the user
    private static String GetUserCommand(Scanner userInput)
    {
       System.out.print("myftp> ");
       String command = userInput.nextLine();
       return command;
    }
    
    //Parses a given command and matches it with possible command options for
    //the client
    private static boolean SelectCommand(String command, MyFTPClient client)
    {
        String[] parseCmd = command.split(" ");
        
        try
        {
            if(parseCmd.length > 0)
            {
                switch (parseCmd[0]) 
                {
                    case "ls":
                        TryList(client);
                        break;
                    case "quit":
                        client.Quit();
                        return false;
                    case "cd":
                        if(parseCmd.length > 1)
                        {
                            TryCD(client, parseCmd[1]);
                        }
                        else
                        {
                            System.out.println("Missing argument.");
                        }   
                        break;
                    case "get":
                        if(parseCmd.length > 1)
                        {
                            TryGet(client, parseCmd[1]);
                        }
                        else
                        {
                            System.out.println("Missing argument.");
                        }  
                        break;
                    case "put":
                        if(parseCmd.length > 1)
                        {
                            TryPut(client, parseCmd[1]);
                        }
                        else
                        {
                            System.out.println("Missing argument.");
                        }
                        break;
                    case "delete":
                        if(parseCmd.length > 1)
                        {
                            TryDelete(client, parseCmd[1]);
                        }
                        else
                        {
                            System.out.println("Missing argument.");
                        }   
                        break;
                    default:
                        break;
                }
            }
        }
        catch(IOException exception)
        {
            System.out.println(exception);
        }
        
        return true;
    }
    
    //Attemps to use List() method of a given client and prints to the user if
    //it was successful
    private static void TryList(MyFTPClient client) throws IOException
            , UnknownHostException
    {
        if(client.List())
         {
            System.out.println("ls successful");
         }
         else
         {
             System.out.println("ls failed");
         }
    }
    
    //Attemps to use CD() method of a given client and prints to the user if
    //it was successful
    private static void TryCD(MyFTPClient client, String arg) throws IOException
    {
        if(client.CD(arg))
         {
            System.out.println("cd successful");
         }
         else
         {
             System.out.println("cd failed");
         }
    }
    
    //Attemps to use Get() method of a given client and prints to the user if
    //it was successful along with the bytes transferred
    private static void TryGet(MyFTPClient client, String arg) throws IOException
            ,UnknownHostException
    {
        long bytesTransferred = client.Get(arg);
        if(bytesTransferred != -1)
         {
            System.out.println("get successful, " + bytesTransferred + " bytes"
                + " transferred.");
         }
         else
         {
             System.out.println("get failed");
         }
    }
    
    //Attemps to use Put() method of a given client and prints to the user if
    //it was successful along with the bytes transferred
    private static void TryPut(MyFTPClient client, String arg) throws IOException
            ,UnknownHostException
    {
        File file = new File(arg);
        
        if(file.exists())
        {
            long bytesTransferred = client.Put(file);
            if(bytesTransferred != -1)
            {
                System.out.println("put successful, " + bytesTransferred
                    + " bytes transferred.");
            }
            else
            {
                 System.out.println("put failed");
            }
        }
        else
        {
            System.out.println("failed due to file not found.");
        }
    }
    
    //Attemps to use Delete() method of a given client and prints to the user if
    //it was successful
    private static void TryDelete(MyFTPClient client, String arg) throws 
            IOException
    {
        if(client.Delete(arg))
         {
            System.out.println("delete successful");
         }
         else
         {
            System.out.println("delete failed");
         }
    }
}
