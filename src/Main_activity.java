package src;
/**
 * Main activity to start client or server
 *
 * @author jingruichen
 * @since 2018-11-08
 */
import javax.swing.*;

public class Main_activity {

    public static void main(String[] args) {

        Object[] selectioValues = {"Client","Server"};
        String initialSection = "Client";
        Object selection = JOptionPane.showInputDialog(null, "Open activity as:", "Multi_Client_Chat",
                JOptionPane.QUESTION_MESSAGE, null, selectioValues, initialSection);
        if (selection == null) return;
        if (selection.equals("Server")) {
            String[] arguments = new String[]{};
            new CoreServer().main(arguments);
        } else if (selection.equals("Client")) {
            String IPServer;
            //choose predefined server or manual input
            selectioValues = new Object[]{"Local Server", "Test Remote Server", "Manual Input"};
            initialSection = "Local Server";
            selection = JOptionPane.showInputDialog(null, "Login into server:", "Multi_Client_Chat",
                    JOptionPane.QUESTION_MESSAGE, null, selectioValues, initialSection);
            if(selection.equals("Test Remote Server"))IPServer="chat.chenassert.xyz";
            else if(selection.equals("Local Server"))IPServer="localhost";
            else IPServer = JOptionPane.showInputDialog("Enter the Server ip address");
            String[] arguments = new String[]{IPServer};
            new ChatClient().main(arguments);
        }
    }
}