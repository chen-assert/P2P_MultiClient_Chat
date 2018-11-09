/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static src.MultiThreadServer.threadpools2;


// For every client's connection we call this class
public class ServerThread implements Runnable {
    private String clientName = null;
    private DataInputStream is = null;
    private DataOutputStream os = null;
    private Socket clientSocket;
    private int clientCount = 0;

    public ServerThread(Socket clientSocket, int clientCount) {
        this.clientSocket = clientSocket;
        this.clientCount = clientCount;
    }

    public void send(String text) throws IOException {
        os.writeUTF((text));
        os.flush();
    }

    //@param  exclude  0--broadcast all,1--exclude self
    public void broadcast(String text, int exclude) throws IOException {
        for (int i = 0; i < threadpools2.size(); i++) {
            threadpools2.get(i).send(text);
        }
    }

    public boolean startWithIgnoreCase(String src, String obj) {
        if (obj.length() > src.length()) {
            return false;
        }
        return src.substring(0, obj.length()).equalsIgnoreCase(obj);
    }


    public void run() {
        try {
            /*
             * Create input and output streams for this client.
             */
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            String name;
            os.writeUTF("Enter your name.");
            os.flush();
            name = is.readUTF().trim();
            clientName = name;
            os.writeUTF("Welcome " + name
                    + " to our chat room.\nTo leave enter STOP in a new line.");
            os.flush();
            synchronized (ServerThread.class) {
                for (int i = 0; i < threadpools2.size(); i++) {
                    if (threadpools2.get(i) != null && threadpools2.get(i) != this) {
                        threadpools2.get(i).send("A new user " + name + " entered the chat room");
                    }
                }
            }
            /* Start the conversation. */
            while (true) {
                String line = is.readUTF();
                if (line.equals("STOP")) {
                    break;
                }
                /* If the message is private sent it to the given client. */
                /* The message is public, broadcast it to all other clients. */
                if (startWithIgnoreCase(line,"BROADCAST")) {
                    synchronized (ServerThread.class) {
                        String pattern = "BROADCAST\\s*-\\s*(.*)\\s*";
                        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                        Matcher m = r.matcher(line);
                        if (m.find()) {
                            broadcast("<" + name + "> " + m.group(1), 0);
                        }
                    }
                    continue;
                }
                if (startWithIgnoreCase(line,"LIST")) {
                    synchronized (ServerThread.class) {
                        for (int i = 0; i < threadpools2.size(); i++) {
                            ServerThread t = threadpools2.get(i);
                            send(String.format("%sUsername:%s/ID:%s\t\t(%s)", t == this ? "*" : "", t.clientName, t.clientCount, t.clientSocket));
                        }
                    }
                    continue;
                }
                if (startWithIgnoreCase(line,"KICK")) {
                    String pattern = "KICK\\s*-\\s*(.*)\\s*";
                    Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher m = r.matcher(line);
                    int flag = 0;
                    if (m.find()) {
                        for (int i = 0; i < threadpools2.size(); i++) {
                            String kuser = m.group(1);
                            if (kuser.equals(threadpools2.get(i).clientName) || kuser.equals(String.valueOf(threadpools2.get(i).clientCount))) {
                                broadcast(kuser + " has been kicked!", 0);
                                threadpools2.get(i).send("@KICK");
                            }
                        }
                        if (flag == 0) {
                            send("can't find user {" + m.group(1) + "}");
                        }
                    } else {
                        send("invalid command, please input HELP to see command list");
                    }

                    continue;
                }

                if (startWithIgnoreCase(line,"STATS")) {

                    continue;
                }

                send("invalid command, please input HELP to see command list");
            }


            synchronized (ServerThread.class) {
                for (int i = 0; i < threadpools2.size(); i++) {
                    if (threadpools2.get(i) != null && threadpools2.get(i) != this) {
                        threadpools2.get(i).send("*** The user " + name + " is leaving the chat room ***");
                    }
                }
            }
            send("Bye~" + name);
            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized (ServerThread.class) {
                threadpools2.remove(this);
            }
            /*
             * Close the output stream, close the input stream, close the socket.
             */
            is.close();
            os.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            threadpools2.remove(this);
            return;
        }
    }
}
