package myftp;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;

public class MyFTPClient 
{
    private Socket socket = null; //Socket for FTP Connection
    private BufferedReader bReader = null; //Reads from remot  server
    private BufferedWriter bWriter = null; //Writes to  remote server
    private final int DATA_BUFFER_SIZE = 4096; //data buffer for file transfer
    
    //Constructor for MyFTPClient takes a String port and int host as parameters
    public MyFTPClient(String host, int port) throws IOException
            , UnknownHostException
    {
        final int SERVER_CONNECT_CODE = 220;
        socket = new Socket(host, port);
        bReader = new BufferedReader( 
                    new InputStreamReader(socket.getInputStream()));
        bWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
        
        if(!CheckResponseCode(SERVER_CONNECT_CODE))
        {
            throw new IOException("Unexpected response recieved when trying to"
                    + "connect to server");
        }
    }
    
    //Logs in to the remote server
    public boolean Login(String user, String pass) throws IOException
    {
        final int USER_ACCEPT_CODE = 331;
        final int PASS_ACCEPT_CODE = 230;
        
        SendToServer("USER " + user);
        
        if(!CheckResponseCode(USER_ACCEPT_CODE))
        {
            return false;
        }
        
        SendToServer("PASS " + pass);
        
        return CheckResponseCode(PASS_ACCEPT_CODE);
    }
    
    //Lists the files in the current directory of the server
    public boolean List() throws IOException, UnknownHostException
    {
        final int LIST_TRANSFER_CODE = 150;
        final int LIST_SUCCESS_CODE = 226;
        
        Socket newSocket = GetDataSocket("LIST");
        
        if(newSocket == null)
        {
            return false;
        }
        else if(!CheckResponseCode(LIST_TRANSFER_CODE))
        { 
            newSocket.close();
            return false;
        }
        
        BufferedReader dataReader = new BufferedReader(new InputStreamReader(
                newSocket.getInputStream()));
        String response = null;
        
        do
        {
            response = dataReader.readLine();
            
            if(response != null)
            {
                System.out.println(response);
            }
        }
        while(response != null);
        
        dataReader.close();
        newSocket.close();
        return CheckResponseCode(LIST_SUCCESS_CODE);
    }
    
    //Uploads s file to the remote server
    public long Put(File file) throws IOException, UnknownHostException
    {
        final int FILE_TRANSFER_CODE = 150;
        final int TRANSFER_SUCCESS_CODE = 226;
        
        if(file.isDirectory())
        {
            return -1;
        }
        
        BufferedInputStream fileInput = new BufferedInputStream(
                new FileInputStream(file));
        Socket newSocket = GetDataSocket("STOR " + file.getName());
        
        if(newSocket == null)
        {
            fileInput.close();
            return -1;
        }
        else if(!CheckResponseCode(FILE_TRANSFER_CODE))
        {
            newSocket.close();
            fileInput.close();
            return -1;
        }
        
        BufferedOutputStream dataOutput = new BufferedOutputStream(
                newSocket.getOutputStream());
        byte[] dataBuffer = new byte[DATA_BUFFER_SIZE];
        int bytesRead;
        
        while((bytesRead = fileInput.read(dataBuffer)) != -1)
        {
            dataOutput.write(dataBuffer, 0, bytesRead);
        }
        
        dataOutput.flush();
        dataOutput.close();
        fileInput.close();
        newSocket.close();
        
        if(CheckResponseCode(TRANSFER_SUCCESS_CODE))
        {
            return file.length();
        }
        else
        {
            return -1;
        }
    }
    
    //Downloads a file from the remote server
    public long Get(String line) throws IOException, UnknownHostException
    {
        final int FILE_TRANSFER_CODE = 150;
        final int TRANSFER_SUCCESS_CODE = 226;
        
        File file = new File(line);
        file.createNewFile();
        
        BufferedOutputStream fileOutput = new BufferedOutputStream(
                new FileOutputStream(file));
        Socket newSocket = GetDataSocket("RETR " + file.getName());
        
        if(newSocket == null)
        {
            fileOutput.close();
            return -1;
        }
        else if(!CheckResponseCode(FILE_TRANSFER_CODE))
        {
            newSocket.close();
            fileOutput.close();
            return -1;
        }
        
        BufferedInputStream dataInput = new BufferedInputStream(
                newSocket.getInputStream());
        byte[] dataBuffer = new byte[DATA_BUFFER_SIZE];
        int bytesRead;
        
        while((bytesRead = dataInput.read(dataBuffer)) != -1)
        {
            fileOutput.write(dataBuffer, 0, bytesRead);
        }
        
        fileOutput.flush();
        fileOutput.close();
        dataInput.close();
        newSocket.close();
        
        if(CheckResponseCode(TRANSFER_SUCCESS_CODE))
        {
            return file.length();  
        }
        else
        {
            return -1;
        }
    }
    
    //Deletes a designated file in the current directory of the remote server
    public boolean Delete(String line) throws IOException
    {
        final int DELETE_SUCCESS_CODE = 250;
        SendToServer("DELE " + line);
        return CheckResponseCode(DELETE_SUCCESS_CODE);
    }
    
    //Changes the current directory of the remote server
    public boolean CD(String line) throws IOException
    {
        final int CD_SUCCESS_CODE = 250;
        SendToServer("CWD " + line);
        return CheckResponseCode(CD_SUCCESS_CODE);
    }
    
    //Disconnects from the remote server
    public void Quit() throws IOException
    {
        SendToServer("QUIT");
        socket = null;
    }
    
    //Sends a command to the remote server then flushes the BufferedWriter
    private void SendToServer(String line) throws IOException
    {
        if(socket != null)
        {
            try
            {
                bWriter.write(line + "\r\n");
                bWriter.flush();
            }
            catch(IOException exception)
            {
                socket = null;
                throw exception;
            }
        }
        else
        {
            throw new IOException("Not connected to a server.");
        }
    }
    
    //Listens for at response from the remote server
    private String ReadFromServer() throws IOException
    {
        String response = bReader.readLine();
        
        if(response == null)
        {
            throw new IOException("Server has disconnected");
        }
        
        return response;
    }
    
    //Activates passive mode and returns a data Socket
    private Socket GetDataSocket(String command) throws IOException
            , UnknownHostException
    {
        final int PORT_MULTIPLIER = 256;
        final int PASV_SUCCESS_CODE = 227;
        
        SendToServer("PASV");
        String response = GetMatchingResponseCode(PASV_SUCCESS_CODE);
        
        if(response == null)
        {
            return null;
        }
        
        String newIP = null;
        int newPort = -1;
        int start = response.indexOf('(') + 1;
        int end = response.indexOf(')', start);
        
        if(end > 0)
        {
            String socketInfo = response.substring(start, end);
            StringTokenizer tokenizer = new StringTokenizer(socketInfo, ",");
            newIP = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                    + tokenizer.nextToken() + "." + tokenizer.nextToken();
            newPort = (Integer.parseInt(tokenizer.nextToken()) 
                   * PORT_MULTIPLIER) + Integer.parseInt(tokenizer.nextToken());
        }
        
        SendToServer(command);
        return new Socket(newIP, newPort);
    }
    
    //Checks if a response from the remote server has a matching code to a given
    //code
    private boolean CheckResponseCode(int code) throws IOException
    {
        String response = ReadFromServer();
        
        return response.startsWith(code + " ");
    }
    
    //Returns the response from the remote server as a String if it has a 
    //matching code
    private String GetMatchingResponseCode(int code) throws IOException
    {
        String response = ReadFromServer();
        
        if(response.startsWith(code + " "))
        {
            return response;
        }
        else
        {
            return null;
        }
    }
}
