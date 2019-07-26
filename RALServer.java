/**
 * Registered Agency List Server listener
 *
 */
package ralserver;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class RALServer implements Runnable
{
	private static Map<String, String> registeredAgencyList = new HashMap<String, String>();

	private RALServerThread clients[] = new RALServerThread[25];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;

	public static void registerAgency(String host, String agencyName)
	{
		registeredAgencyList.put(host, agencyName);
	}
	public static void unregisterAgency(String host)
	{
		registeredAgencyList.remove(host);
	}
	public static String getAgencyByHost(String host)
	{
		return registeredAgencyList.get(host);
	}

	public RALServer(int port)
	{
	   try
       {
		   System.out.println("Binding to port " + port + ", please wait  ...");
		   server = new ServerSocket(port);
		   System.out.println("Server started: " + server);
		   start();
	   }
	   catch(IOException e)
	   {
		   System.out.println("Error starting server at port " + port + ": " + e.getMessage());
	   }
	}
	public void run()
	{
	   while (thread != null)
	   {
		   try
		   {
			   System.out.println("Waiting for a client ...");
			   addThread(server.accept());
		   }
		   catch(IOException e)
		   {
			   System.out.println("Error accepting client: " + e);
			   stop();
		   }
	   }
	}

	private int findClient(int ID)
	{
	   for (int i = 0; i < clientCount; i++)
		   if (clients[i].getID() == ID)
			   return i;
	   return -1;
	}
	public synchronized void handle(int ID, String input)
	{
	   if (input.equals(".bye"))
	   {
		   clients[findClient(ID)].sendMessage(".bye");//send msg to client
		   deleteThread(ID);
	   }
	   else if(registeredAgencyList.containsKey(input))//keyexists
		   clients[findClient(ID)].sendMessage(getAgencyByHost(input));
	   else
		   clients[findClient(ID)].sendMessage("Unregistered");
	}
	public synchronized void deleteThread(int ID)
	{
	   int pos = findClient(ID);
	   if (pos >= 0)
	   {
		   RALServerThread toTerminate = clients[pos];
		   System.out.println("Removing client thread " + ID + " at " + pos);
		   if (pos < clientCount-1)
		   {
			   for (int i = pos+1; i < clientCount; i++)
				   clients[i-1] = clients[i];
		   }
		   clientCount--;
		   try
		   {
			   toTerminate.close();
		   }
		   catch(IOException ioe)
		   {
			   System.out.println("Error closing thread: " + ioe);
		   }
		   toTerminate.stop();
	   }
	}
	private void addThread(Socket socket)
	{
	   if (clientCount < clients.length)
	   {
		   System.out.println("Client accepted: " + socket);
		   clients[clientCount] = new RALServerThread(this, socket);
		   try
		   {
			   clients[clientCount].open();
			   clients[clientCount].start();
			   clientCount++;
		   }
		   catch(IOException e)
		   {
			   System.out.println("Error opening thread: " + e);
		   }
	   }
	   else
		   System.out.println("Unable to add more clients, maximum " + clients.length + " reached.");
   	}

   	public void start()
   	{
   		if (thread == null)
   		{
   			thread = new Thread(this);
   			thread.start();
   		}
   	}
   	public void stop()
   	{
   		if (thread != null)
   		{
   			thread.stop();
   			thread = null;
   		}
   	}
   	public static void main(String args[])
   	{
   		RALServer myServer = new RALServer(5999);
   		registerAgency("10.0.0.1", "AgencyX");//register h1
   		registerAgency("10.0.1.2", "AgencyY");//register h4
   	}
}
