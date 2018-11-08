/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import static src.MultiThreadServer.maxClientsCount;
import static src.MultiThreadServer.threadpools;

/**
 * @author mohammed
 */


// For every client's connection we call this class
public class ServerThread implements Runnable {
    private String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket;
    private int thnum=0;

    public ServerThread(Socket clientSocket,int thnum) {
        this.clientSocket = clientSocket;
        this.thnum=thnum;
    }

    public void run() {
        try {
            /*
             * Create input and output streams for this client.
             */
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            String name;
            while (true) {
                os.println("Enter your name.");
                name = is.readUTF().trim();
                if (name.indexOf('@') == -1) {
                    clientName="@"+name;
                    break;
                } else {
                    os.println("The name should not contain '@' character.");
                }
            }

            /* Welcome the new the client. */
            os.println("Welcome " + name
                    + " to our chat room.\nTo leave enter /quit in a new line.");
            synchronized(ServerThread.class) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threadpools[i] != null && threadpools[i] != this) {
                        threadpools[i].os.println("*** A new user " + name
                                + " entered the chat room !!! ***");
                    }
                }
            }
            /* Start the conversation. */
            while (true) {
                String line = is.readUTF();
                if (line.startsWith("/quit")) {
                    break;
                }
                /* If the message is private sent it to the given client. */
                if (line.startsWith("@")) {
                    String[] words = line.split("\\s", 2);
                    if (words.length > 1 && words[1] != null) {
                        words[1] = words[1].trim();
                        if (!words[1].isEmpty()) {
                            synchronized(ServerThread.class) {
                                for (int i = 0; i < maxClientsCount; i++) {
                                    if (threadpools[i] != null && threadpools[i] != this
                                            && threadpools[i].clientName != null
                                            && threadpools[i].clientName.equals(words[0])) {
                                        threadpools[i].os.println("<" + name + "> " + words[1]);
                                        /*
                                         * Echo this message to let the client know the private
                                         * message was sent.
                                         */
                                        this.os.println(">" + name + "> " + words[1]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    /* The message is public, broadcast it to all other clients. */
                    synchronized(ServerThread.class) {
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threadpools[i] != null && threadpools[i].clientName != null) {
                                threadpools[i].os.println("<" + name + "> " + line);
                            }
                        }
                    }
                }
            }
            synchronized(ServerThread.class) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threadpools[i] != null && threadpools[i] != this
                            && threadpools[i].clientName != null) {
                        threadpools[i].os.println("*** The user " + name
                                + " is leaving the chat room !!! ***");
                    }
                }
            }
            os.println("*** Bye " + name + " ***");

            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized(ServerThread.class) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threadpools[i] == this) {
                        threadpools[i] = null;
                    }
                }
            }
            /*
             * Close the output stream, close the input stream, close the socket.
             */

            is.close();
            os.close();
            clientSocket.close();

        } catch (IOException e) {
        }
    }
}
