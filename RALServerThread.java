/**
 * Registered Agency List Server Thread
 * 
 */
package ralserver;

import java.net.*;
import java.io.*;


public class RALServerThread extends Thread
{
	private RALServer server    = null;
	private Socket socket    = null;
	private int threadID        = -1;
	private DataInputStream  sockIn  =  null;
	private DataOutputStream sockOut = null;

	public RALServerThread(RALServer _server, Socket _socket)
	{
		super();
	    server = _server;
	    socket = _socket;
	    threadID = socket.getPort();
	}
	public void sendMessage(String msg)
	{
		try
	    {
			sockOut.writeUTF(msg);
			sockOut.flush();
	    }
	    catch(IOException e)
	    {
	    	System.out.println(threadID + " ERROR sending: " + e.getMessage());
	        server.deleteThread(threadID);
	        stop();
	    }
	}
	public int getID()
	{
		return threadID;
	}
	public void run()
	{
		System.out.println("Server Thread " + threadID + " running.");
		while (true)
		{
			try
		    {
				server.handle(threadID, sockIn.readUTF());
		    }
		    catch(IOException e)
		    {
		    	System.out.println(threadID + " ERROR reading: " + e.getMessage());
		        server.deleteThread(threadID);
		        stop();
		    }
		}
	}
	public void open() throws IOException
	{
		sockIn = new DataInputStream(new
	                        BufferedInputStream(socket.getInputStream()));
		sockOut = new DataOutputStream(new
	                        BufferedOutputStream(socket.getOutputStream()));
	}
	public void close() throws IOException
	{
		if (socket != null)
			socket.close();
	    if (sockIn != null)
	    	sockIn.close();
	    if (sockOut != null)
	    	sockOut.close();
	}
}
