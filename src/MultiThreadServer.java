

package src;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author mohammed
 */

// the Server class
public class MultiThreadServer {
    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;

    // This chat server can accept up to maxClientsCount clients' connections.
    protected static final int maxClientsCount = 100;
    protected static final ServerThread[] threadpools = new ServerThread[maxClientsCount];


    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setSize(800,600);
        frame.setTitle("MyChatApp - server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);


        // The default port number.
        int portNumber;
        if (args.length >= 1) {
            portNumber = Integer.valueOf(args[0]).intValue();
        } else {
            portNumber = 2222;
        }
        System.out.println("Usage: java MultiThreadServer <portNumber>\n"
                + "Now using port number=" + portNumber);


        /*
         * Open a server socket on the portNumber (default 2222). Note that we can
         * not choose a port less than 1023 if we are not privileged users (root).
         */
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        /*
         * Create a client socket for each connection and pass it to a new client
         * thread.
         */
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threadpools[i] == null) {
                        threadpools[i] = new ServerThread(clientSocket,i);
                        Thread t = new Thread(threadpools[i]);
                        t.start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
