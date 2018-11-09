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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static src.MultiThreadServer.threadpools2;


// For every client's connection we call this class
public class ServerThread implements Runnable {
    private String clientName = null;
    private DataInputStream is = null;
    private DataOutputStream os = null;
    private Socket clientSocket;
    private int clientCount;
    protected Vector<String> hists;

    public ServerThread(Socket clientSocket, int clientCount) {
        this.clientSocket = clientSocket;
        this.clientCount = clientCount;
        hists = new Vector<>();
    }

    public void send(String text) throws IOException {
        synchronized (this) {
            os.writeUTF((text));
            os.flush();
        }
    }

    //@param  exclude  0--broadcast all,1--exclude self
    public void broadcast(String text, int exclude) throws IOException {
        for (ServerThread aThreadpools2 : threadpools2) {
            if (exclude == 1 && aThreadpools2 == this) {

            } else {
                aThreadpools2.send(text);
            }

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
            send("Welcome " + name
                    + " to our chat room.\nTo leave enter STOP in a new line.");
            broadcast("A new user " + name + " entered the chat room", 1);
            /* Start the conversation. */
            while (true) {
                String line = is.readUTF();
                hists.add(line);
                if(hists.size()>30)hists.remove(0);
                if (line.equals("STOP")) {
                    break;
                }
                /* If the message is private sent it to the given client. */
                /* The message is public, broadcast it to all other clients. */
                if (startWithIgnoreCase(line, "BROADCAST")) {
                    String pattern = "BROADCAST\\s*-\\s*(.*)\\s*";
                    Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher m = r.matcher(line);
                    if (m.find()) {
                        broadcast("<" + name + "> " + m.group(1), 0);
                    } else {
                        send("invalid command, please input HELP to see command list");
                    }
                    continue;
                }
                if (startWithIgnoreCase(line, "LIST")) {
                    for (ServerThread t : threadpools2) {
                        send(String.format("%sUsername:%s/ID:%s\t\t(%s)", t == this ? "*" : "", t.clientName, t.clientCount, t.clientSocket));
                    }
                    continue;
                }
                if (startWithIgnoreCase(line, "KICK")) {
                    String pattern = "KICK\\s*-\\s*(.*)\\s*";
                    Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher m = r.matcher(line);
                    int flag = 0;
                    if (m.find()) {
                        for (ServerThread aThreadpools2 : threadpools2) {
                            String kuser = m.group(1);
                            if (kuser.equals(aThreadpools2.clientName) || kuser.equals(String.valueOf(aThreadpools2.clientCount))) {
                                broadcast(kuser + " has been kicked!", 0);
                                aThreadpools2.send("@KICK");
                                flag = 1;
                                break;
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

                if (startWithIgnoreCase(line, "STATS")) {
                    String pattern = "STATS\\s*-\\s*(.*)\\s*";
                    Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher m = r.matcher(line);
                    int flag = 0;
                    if (m.find()) {
                        for (ServerThread aThreadpools2 : threadpools2) {
                            String suser = m.group(1);
                            if (suser.equals(aThreadpools2.clientName) || suser.equals(String.valueOf(aThreadpools2.clientCount))) {
                                for (String hist :aThreadpools2.hists) {
                                    send(suser+":"+hist);
                                }
                                flag = 1;
                                break;
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

                send("invalid command, please input HELP to see command list");
            }


            synchronized (ServerThread.class) {
                for (ServerThread aThreadpools2 : threadpools2) {
                    if (aThreadpools2 != null && aThreadpools2 != this) {
                        aThreadpools2.send("*** The user " + name + " is leaving the chat room ***");
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
        }
    }
}
