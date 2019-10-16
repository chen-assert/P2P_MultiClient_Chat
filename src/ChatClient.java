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
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import org.json.*;

public class ChatClient {
    //GUI
    static class ChatFrame extends JFrame implements Observer {
        private JTextArea textArea;
        private JTextField inputTextField;
        private JButton sendButton;
        private ChatHandle chatHandle;

        protected  static JTextField inputKeyField;

        public ChatFrame(ChatHandle chatHandle) {
            this.chatHandle = chatHandle;
            chatHandle.addObserver(this);
            buildGUI();
        }

        private void buildGUI() {
            /*
                    GUI struct:
                    key:-------
                    ***********
                    *********** <-TextArea
                    ***********
                    --------###
             inputTextField|Send button
             */
            JLabel label = new JLabel();
            label.setText("key(len>=8):");
            Box box0 = Box.createHorizontalBox();
            add(box0, BorderLayout.NORTH);
            inputKeyField = new JTextField();
            box0.add(label);
            box0.add(inputKeyField);
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
                String key = inputKeyField.getText();
                if (str != null && str.trim().length() > 0) {
                    chatHandle.send(str, key);
                }
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
        String server = "localhost";
        if (args[0].length()!= 0) {
            server = args[0];
        }
        String name="nobody";
        if (args[1].length()!= 0) {
            name = args[1];
        }
        int port = 9548;
        ChatHandle chatHandle = new ChatHandle(name);
        JFrame frame = new ChatFrame(chatHandle);
        frame.setSize(800, 600);
        frame.setTitle("Multi_Client_Chat@" + server + ":" + port);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        ((ChatFrame) frame).inputTextField.requestFocus();
        try {
            chatHandle.InitSocket(server, port);
        } catch (IOException ex) {
            System.out.println("Cannot connect to " + server + ":" + port);
            ex.printStackTrace();
            System.exit(0);
        }
    }
}