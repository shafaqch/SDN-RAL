/**
 * Registered Agency List Client
 * to be run on the floodlight controller
 * in the same package as the SDN reactive module
 */
package net.floodlightcontroller.headerextract;

import java.net.*;
import java.io.*;


public class RALClient2
{
	private Socket socket = null;
	private DataOutputStream sockOut = null;
	private DataInputStream  sockIn = null;
	private String queryString = null;
	private String queryResponse = null;

	public RALClient2(String serverName, int serverPort, String qryString)//
	{
		System.out.println("Establishing connection to RAL Server. Please wait ...");
		try
		{
			socket = new Socket(serverName, serverPort);
			queryString = qryString;
			System.out.println("Connected to RAL Server: " + socket);
			start();
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe)
		{
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}

		//write once and read once
		try
		{
			//streamOut.writeUTF("what is x");
			sockOut.writeUTF(queryString);
			sockOut.flush();

			sockIn  = new DataInputStream(socket.getInputStream());
			queryResponse = sockIn.readUTF();
			System.out.println(queryResponse);
		}
		catch(IOException e)
		{
			System.out.println("Error sending message: " + e.getMessage());
		}

       // System.out.println("Closing socket");//added
        //stop();//added
	}

	public String getQueryResponse()
	{
		return queryResponse;
	}
	public void start() throws IOException
	{
		sockIn  = new DataInputStream(socket.getInputStream());
		sockOut = new DataOutputStream(socket.getOutputStream());
	}

	public void stop()
	{
		try
		{

			if (sockOut != null)  sockOut.close();
			if (sockIn  != null)  sockIn.close();
			if (socket  != null)  socket.close();
		}

		catch(IOException ioe)
		{
			System.out.println("Error closing ...");
		}
	}

	public void bye()
	{
		try
		{
			sockOut.writeUTF(".bye");
			sockOut.flush();
		}
		catch(IOException e)
		{
			System.out.println("Error sending message: " + e.getMessage());
		}
        System.out.println("Closing socket");//added
        stop();//added
	}

	public static void main(String args[])
	{
		RALClient2 client = null;
   		client = new RALClient2("localhost",5999,"10.0.0.1");//third argument registers host

	}
}
