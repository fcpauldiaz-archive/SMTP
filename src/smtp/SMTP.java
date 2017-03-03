/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smtp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import javax.swing.JFrame;


/**
 *
 * @author SDX
 */
public class SMTP {
    
    public static DebugServer window;
    public static DebugMail windowMail;
    public static boolean debug = true;
    public static int clientID = 0;
    public static String mailServer = "ngrok.com";
    public static DebugUsers windowUser;

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
        
        windowMail = new DebugMail();
        windowMail.setSize(800, 450);
        windowMail.setLocation(0, 300);
        windowMail.setVisible(true);
        windowMail.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MultiThreadedServer server = new MultiThreadedServer(12000, 8);
        GetMailServer getMail = new GetMailServer(2507, 8);
        new Thread(server).start();
        new Thread(getMail).start();

        try {
            FileInputStream fileIn = new FileInputStream("data/emails.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ArrayList<Email> emails = (ArrayList<Email>) in.readObject();
            for (int i = 0; i < emails.size(); i++) {
                Email email = emails.get(i);
               windowMail.addRow(email.getUserFrom(), email.getUserTo().getEmailAdress(), email.getSubject(), email.getMessage(), email.getTime());
            }
            in.close();
            fileIn.close();
        }catch(IOException i) {

        }catch(ClassNotFoundException c) {

        }
        
        
          
        windowUser = new DebugUsers();
        windowUser.setSize(450, 450);
        windowUser.setLocation(450, 150);
        windowUser.setVisible(true);
        windowUser.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            FileInputStream fileIn = new FileInputStream("data/users.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ArrayList<User> users = (ArrayList<User>) in.readObject();
            for (int i = 0; i < users.size(); i++) {
               User user = users.get(i);
               windowUser.addRow(user.getEmailAdress());
            }
            in.close();
            fileIn.close();
         }catch(IOException i) {
            i.printStackTrace();
            return;
         }catch(ClassNotFoundException c) {
            System.out.println("User Class not found");
            c.printStackTrace();
            return;
         }
        
       
        
    }
    
}
