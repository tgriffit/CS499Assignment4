//http://www.binarytides.com/java-socket-programming-tutorial/
import java.io.*;
import java.util.ArrayList;
import java.net.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Point {
	public double x;
	public double y;
	
	public Point(double _x, double _y) {
		this.x = _x;
		this.y = _y;
	}
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	public boolean equals(Object o) {
		Point p = (Point) o;
		if (p.x == this.x && p.y == this.y)
			return true;
		
		return false;
	}
}

public class TrackerReader extends Thread
{
    public volatile double x;
    public volatile double y;
    public volatile double a;
    public volatile double theta;
    public volatile double targetx;
    public volatile double targety;
    public volatile double lastpointx;
    public volatile double lastpointy;
    public volatile ArrayList<Point> points;

    //public static void main(String args[])
    public void run()
    {
        x = 0;
        y = 0;
        System.out.println("LISTENER");
        ServerSocket s = null;
        Socket conn = null;
        points = new ArrayList<Point>();

        try
        {
            //1. creating a server socket - 1st parameter is port number and 2nd is the backlog
            s = new ServerSocket(5000 , 10);

            //2. Wait for an incoming connection
            echo("Server socket created.Waiting for connection...");

            conn = s.accept();

            //print the hostname and port number of the connection
            echo("Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());
            String line , input = "";
            JSONParser parser=new JSONParser();
            try
            {
                //get socket writing and reading streams
                DataInputStream in = new DataInputStream(conn.getInputStream());
                PrintStream out = new PrintStream(conn.getOutputStream());

                //Send welcome message to client
                //out.println("Welcome to the Server");

                //Now start reading input from client
                while((line = in.readLine()) != null && !line.equals("."))
                {
                    try{
                        //System.out.println(line);
                        Object obj=parser.parse(line);
                        JSONObject jsonObject = (JSONObject) obj;
                        x = (Double) jsonObject.get("x");
                        y = (Double) jsonObject.get("y");
                        a = (Double) jsonObject.get("a");
                        theta = (Double) jsonObject.get("theta");
                        targetx = (Double) jsonObject.get("targetx");
                        targety = (Double) jsonObject.get("targety");
                        long pointx = (Long) jsonObject.get("pointx");
                        long pointy = (Long) jsonObject.get("pointy");
                        addNewPoint(pointx, pointy);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                //client disconnected, so close socket
                conn.close();
            }
            catch (IOException e)
            {
                System.out.println("IOException on socket : " + e);
                e.printStackTrace();
            }

        }

        catch(IOException e)
        {
            System.err.println("IOException");
        }

        //5. close the connections and stream
        try
        {
            s.close();
        }

        catch(IOException ioException)
        {
            System.err.println("Unable to close. IOexception");
        }
    }
    
    private void addNewPoint(long x, long y) {
    	// bad hardcoding of special init case
    	if (points.size() == 0 && x == 0 && y == 0)
    		return;
    	
    	// if the point is new, add it, else ignore it
        if (!points.contains(new Point(x, y))){
        	lastpointx = x;
        	lastpointy = y;
        	points.add(new Point(x, y));
        }
    }
    
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
