package src;

import javax.swing.*;

//Main activity to choose start client or server
public class Main_activity {


    public static void main(String[] args) {

        Object[] selectioValues = {"Server", "Client"};
        String initialSection = "Server";

        Object selection = JOptionPane.showInputDialog(null, "Login as : ", "Multi_Client_Chat",
                JOptionPane.QUESTION_MESSAGE, null, selectioValues, initialSection);
        if (selection == null) return;
        if (selection.equals("Server")) {
            String[] arguments = new String[]{};
            new CoreServer().main(arguments);
        } else if (selection.equals("Client")) {
            String IPServer = JOptionPane.showInputDialog("Enter the Server ip address");
            String[] arguments = new String[]{IPServer};
            new ChatClient().main(arguments);
        }

    }

}
