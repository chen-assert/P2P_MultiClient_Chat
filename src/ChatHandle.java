package src;
/**
 * the chat handle module to control the client's communication with server
 *
 * @author jingruichen
 * @since 2018-11-12
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static src.ChatClient.ChatFrame.inputKeyField;
import static src.PostHandle.PostRequest;
import static src.ServerThread.KICK;
import static src.ServerThread.startWithIgnoreCase;

//using observer model to manage text flash
class ChatHandle extends Observable {
    //send a new message
    public void send(String text, String key) {
        if (key == null) {
            //return;
        }
        //handle HELP command in local
        if (text.equalsIgnoreCase("HELP")) {
            String str = text + "\n" +
                    "  - command {BROADCAST - {content}} enables a client to send text to all the other clients connected to the server;\n" +
                    "  - command {!STOP} forces the server to close the connection with the client that initiated the command, this event must be announced to all other clients;\n" +
                    "  - command {!LIST} displays a list of all client IDs currently connected to the server;\n" +
                    "  - command {!KICK - ID} closes the connection between the server and the IP client, and also announces this to all clients;\n" +
                    "  - command {!STATS - ID} gets a list of all commands used by the client identified by the ID.\n";
            notifyObservers(str);
            return;
        } else if (text.equalsIgnoreCase("!STOP")) {
            notifyObservers("You have left from the chat room, and the window would stop in 5 seconds.");
            new exit_().start();
            return;
        }
        //try to send message
        try {
            if (startWithIgnoreCase(text, "BROADCAST")) {
                String pattern = "BROADCAST\\s*-\\s*(.*)\\s*";
                Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher m = r.matcher(text);
                //parse the input content
                if (m.find()) {
                    try {
                        String se = PostRequest(1, m.group(1), key);
                        text = "BROADCAST-" + se;
                        outputStream.writeUTF(text);
                        outputStream.flush();
                    } catch (PostHandle.RequestFailException e) {
                        notifyObservers("Encrypt fail?");
                    }
                }
            } else {
                outputStream.writeUTF(text);
                outputStream.flush();
            }
            //outputStream.writeUTF(new PBE_Encrypt().encode(text));
        } catch (Exception ex) {
            notifyObservers(ex);
        }
    }

    //receive a new message
    public void receive() throws Exception {
        DataInputStream is = new DataInputStream(socket.getInputStream());
        String line;
        while (true) {
            //line = new PBE_Encrypt().decode(is.readUTF());
            line = is.readUTF();
            if (line.equals(KICK)) {
                //receive KICK command
                notifyObservers("you have been kicked!!");
                close();
                break;
            }
            notifyObservers(line);
            String pattern = "<(.*)>\\s*(.*)\\s*";
            Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = r.matcher(line);
            if (m.find() && !m.group(1).equals(name)) {
                notifyObservers("Receive message:" + m.group(2));
                try {
                    notifyObservers("Decrypting...");
                    String de = PostRequest(2, m.group(2), inputKeyField.getText());
                    notifyObservers("The message is:" + de);
                } catch (PostHandle.RequestFailException e) {
                    notifyObservers("Decrypt fail?");
                }
            }
        }
    }

    //close the windows after 10 seconds
    class exit_ extends Thread {
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    private Socket socket;
    private DataOutputStream outputStream;

    @Override
    public void notifyObservers(Object arg) {
        super.setChanged();
        super.notifyObservers(arg);
    }

    public String name;

    public ChatHandle(String name) {
        this.name = name;
    }

    public void InitSocket(String server, int port) throws IOException {
        try {
            notifyObservers("Try to connect to server@" + server + ":" + port);
            socket = new Socket();
            socket.connect(new InetSocketAddress(server, port), 10000);
        } catch (Exception e) {
            notifyObservers("Connection fail");
            e.printStackTrace();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.exit(0);
        }
        notifyObservers("Connection success");
        new Heart(this).start();
        outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.writeUTF(name);
        outputStream.flush();
        try {
            receive();
        } catch (Exception e) {
            //put the exception to screen
            notifyObservers(e);
        }
    }
    //Close the socket

    public void close() {
        try {
            send("@STOP", null);
            socket.close();
        } catch (IOException ex) {
            notifyObservers(ex);
        }
    }
}
