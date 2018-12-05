package src;
/**
 * client side of the chat system
 *
 * @author jingruichen
 * @since 2018-11-08
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.Observer;


public class ChatClient {
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
            /*
                    GUI struct:
                    ***********
                    *********** <-TextArea
                    ***********
                    --------###
             inputTextField|Send button
             */
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
                if (str != null && str.trim().length() > 0) {
                    chatHandle.send(str);
                }
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

        //push the object to the screen(maybe chat information or exception information)
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