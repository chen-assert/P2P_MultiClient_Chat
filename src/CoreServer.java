package src;
/**
 * core server module to allocate server thread to every user
 *
 * @author jingruichen
 * @since 2018-11-08
 */
import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class CoreServer {
    private static ServerSocket serverSocket = null;
    //Client limits
    protected static final int maxClientsCount = 100;
    protected static Vector<ServerThread> threadpool = new Vector();

    public static void main(String args[]) {
        try {
            JFrame frame = new JFrame();
            frame.setSize(800, 600);
            frame.setTitle("Multi_Client_Chat@server");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            frame.setVisible(true);
        } catch (Exception e) {
            //make the problem can running in headless service
            //window is unnecessary
            e.printStackTrace();
        }
        int portNumber;
        if (args.length >= 1) {
            portNumber = Integer.valueOf(args[0]);
        } else {
            portNumber = 9548;
        }
        System.out.println("Server start in port " + portNumber);
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //user count
        int count = 1;
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(clientSocket, count++);
                Thread t = new Thread(serverThread);
                t.start();
                //use thread pool to save all linked client
                threadpool.add(serverThread);
                if (threadpool.size() >= maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("ERROR:Server's connection reach to the limit");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}