package src;
/**
 * sending heart package regularly to avoid linking reset
 *
 * @author jingruichen
 * @since 2018-11-15
 */
public class Heart extends Thread {
    static String HEART="@HEART@";
    ChatHandle chatHandle;
    public Heart(ChatHandle chatHandle){
        this.chatHandle=chatHandle;
    }
    public void run() {
        while (true) {
            try {
                sleep(60000);
                chatHandle.send(HEART,null);
            } catch (InterruptedException e) {
                e.printStackTrace();
                chatHandle.close();
                break;
            }
        }
    }
}

