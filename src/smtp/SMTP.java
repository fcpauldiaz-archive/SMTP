/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smtp;

import javax.swing.JFrame;


/**
 *
 * @author SDX
 */
public class SMTP {
    
    public static DebugServer window;
    public static boolean debug = true;
    public static int clientID = 0;
    public static String mailServer = "ngrok.com";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        window = new DebugServer();
        window.setSize(450, 450);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MultiThreadedServer server = new MultiThreadedServer(2407, 5);
        GetMailServer getMail = new GetMailServer(2507, 5);
        new Thread(server).start();
        new Thread(getMail).start();

        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }
    
}
