package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Observer;


public class ChatClient {
    //using observer model to manage text flash
    static class ChatHandle extends Observable {
        private Socket socket;
        private DataOutputStream outputStream;
        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }
        public void InitSocket(String server, int port) throws IOException {
            try {
                notifyObservers("Try to connect to server@" + server + ":" + port);
                socket = new Socket();
                socket.connect(new InetSocketAddress(server,port),10000);
            }catch (Exception e){
                notifyObservers("Connection fail");
                e.printStackTrace();
                try{
                Thread.sleep(2000);}
                catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);

            }
            notifyObservers("Connection success");

            //socket = new Socket(server, port);
            outputStream = new DataOutputStream(socket.getOutputStream());
            try {
                receive();
            } catch (IOException e) {
                //put the exception to screen
                notifyObservers(e);
            }
        }


        public void send(String text) {
            //handle HELP command in local
            if(text.equalsIgnoreCase("HELP")){
                String str= text+"\n"+
                            "  - command {BROADCAST - {content}} enables a client to send text to all the other clients connected to the server;\n" +
                            "  - command {STOP} forces the server to close the connection with the client that initiated the command, this event must be announced to all other clients;\n" +
                            "  - command {LIST} displays a list of all client IDs currently connected to the server;\n" +
                            "  - command {KICK - ID} closes the connection between the server and the IP client, and also announces this to all clients;\n" +
                            "  - command {STATS - ID} gets a list of all commands used by the client identified by the ID.\n";
                notifyObservers(str);
                return ;
            }
            try {
                outputStream.writeUTF((text));
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        public void receive() throws IOException {
            DataInputStream is = new DataInputStream(socket.getInputStream());
            String line;
            while (true) {
                line = is.readUTF();
                if (line.equals("@KICK")) {
                    notifyObservers("you have been kicked!!");
                    close();
                    break;
                }
                notifyObservers(line);
            }
        }

        /**
         * Close the socket
         */
        public void close() {
            try {
                send("STOP");
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    //GUI
    static class ChatFrame extends JFrame implements Observer {
        private JTextArea textArea;
        private JTextField inputTextField;
        private JButton sendButton;
        private ChatHandle chatHandle;

        public ChatFrame(ChatHandle chatHandle) {
            this.chatHandle = chatHandle;
            chatHandle.addObserver(this);
            buildGUI();
        }

        private void buildGUI() {
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            sendButton = new JButton("Send");
            box.add(inputTextField);
            box.add(sendButton);
            // Action for the inputTextField and the goButton
            ActionListener sendListener = e -> {
                String str = inputTextField.getText();
                if (str != null && str.trim().length() > 0)
                    chatHandle.send(str);
                inputTextField.requestFocus();
                inputTextField.setText("");
            };
            //using enter keyboard or send button can both trigger event
            inputTextField.addActionListener(sendListener);
            sendButton.addActionListener(sendListener);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatHandle.close();
                }
            });
        }

        //push the object to the screen
        public void update(Observable o, Object arg) {
            SwingUtilities.invokeLater(() -> {
                textArea.append(arg.toString() + "\n");
            });
        }
    }

    public static void main(String[] args) {
        String server = null;
        if (args.length != 0) {
            server = args[0];
        } else {
            server = "localhost";
        }
        int port = 9548;
        ChatHandle chatHandle = new ChatHandle();
        JFrame frame = new ChatFrame(chatHandle);
        frame.setSize(800, 600);
        frame.setTitle("Multi_Client_Chat@" + server + ":" + port);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        try {
            chatHandle.InitSocket(server, port);
        } catch (IOException ex) {
            System.out.println("Cannot connect to " + server + ":" + port);
            ex.printStackTrace();
            System.exit(0);
        }
    }
}