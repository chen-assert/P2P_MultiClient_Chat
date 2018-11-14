package src;
/**
 * server thread to handle single user
 *
 * @author jingruichen
 * @since 2018-11-08
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static src.CoreServer.threadpool;
import static src.Heart.HEART;


// one client correspond to one thread
public class ServerThread implements Runnable {
    private String clientName = null;
    private DataInputStream is = null;
    private DataOutputStream os = null;
    private Socket clientSocket;
    private int clientCount;
    //command history
    protected Vector<String> hists;

    public ServerThread(Socket clientSocket, int clientCount) {
        this.clientSocket = clientSocket;
        this.clientCount = clientCount;
        hists = new Vector<>();
    }

    public void send(String text) throws Exception {
        synchronized (this) {
            os.writeUTF(new PBE_Encrypt().encode(text));
            os.flush();
        }
    }
    public String receive() throws Exception {
        String  line =new PBE_Encrypt().decode(is.readUTF());
        return line;
    }
    /**
    @text the broadcast text
    @exclude  0--broadcast all,1--exclude self
    */
    public void broadcast(String text, int exclude) throws Exception {
        //show the log in server
        System.out.println(text);
        for (ServerThread aThreadpools2 : threadpool) {
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
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            String name;
            send("Please enter your name:");
            name =receive().trim();
            clientName = name;
            //input name
            send("Welcome " + name + " to our chat room.\nTo leave enter STOP in a new line.");
            broadcast("*** A new user " + name + " entered the chat room ***", 1);
            // Start the conversation
            while (true) {
                String line = receive();
                //if receive heart package then do nothing
                if(line.equals(HEART)){
                    continue;
                }
                hists.add(line);
                // at most record 30 line history
                if (hists.size() > 30) hists.remove(0);
                if (line.equals("STOP")) {
                    break;
                }
                if (startWithIgnoreCase(line, "BROADCAST")) {
                    String pattern = "BROADCAST\\s*-\\s*(.*)\\s*";
                    Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher m = r.matcher(line);
                    //parse the input content
                    if (m.find()) {
                        broadcast("<" + name + "> " + m.group(1), 0);
                    } else {
                        send("invalid command, please input HELP to see command list");
                    }
                    continue;
                }
                if (startWithIgnoreCase(line, "LIST")) {
                    //send all users' information
                    for (ServerThread t : threadpool) {
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
                        for (ServerThread aThreadpools2 : threadpool) {
                            String kuser = m.group(1);
                            //match name or id
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
                        for (ServerThread aThreadpools2 : threadpool) {
                            String suser = m.group(1);
                            if (suser.equals(aThreadpools2.clientName) || suser.equals(String.valueOf(aThreadpools2.clientCount))) {
                                //send history
                                for (String hist : aThreadpools2.hists) {
                                    send(suser + ":" + hist);
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

            //clean up
            send("Bye~" + name);
            synchronized (this) {
                threadpool.remove(this);
            }
            broadcast("*** The user " + name + " has left the chat room ***", 0);
            is.close();
            os.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            //avoid potential problem
            threadpool.remove(this);
        }
    }
}
