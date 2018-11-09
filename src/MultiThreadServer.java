

package src;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * @author mohammed
 */

// the Server class
public class MultiThreadServer {
    // The server socket.
    private static ServerSocket serverSocket = null;

    // This chat server can accept up to maxClientsCount clients' connections.
    protected static final int maxClientsCount = 100;
    //protected static final ServerThread[] threadpools = new ServerThread[maxClientsCount];
    protected static Vector<ServerThread> threadpools2 = new Vector();

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setTitle("MyChatApp - server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);

        // The default port number.
        int portNumber;
        if (args.length >= 1) {
            portNumber = Integer.valueOf(args[0]);
        } else {
            portNumber = 9548;
        }
        System.out.println("Usage: java MultiThreadServer <portNumber>\n"
                + "Now using port number=" + portNumber);


        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Create a client socket for each connection and pass it to a new client
         * thread.
         */
        //user count
        int count = 1;
        while (true) {
            try {
                // The client socket.
                Socket clientSocket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(clientSocket, count++);
                Thread t = new Thread(serverThread);
                t.start();
                threadpools2.add(serverThread);
                if (threadpools2.size() >= maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

