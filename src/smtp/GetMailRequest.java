/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 16, 2017
**/

package smtp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SDX
 */
public class GetMailRequest implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText = null;
    protected WorkingQueue cola = null;
    protected ArrayList<Email> emails;
    protected ArrayList<User> users;
    
    
    public GetMailRequest(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.loadUsers();
        this.loadEmails();
        System.out.println(emails);
    }
    
    private void loadUsers() {
        try {
         FileInputStream fileIn = new FileInputStream("data/users.ser");
         ObjectInputStream in = new ObjectInputStream(fileIn);
         this.users = (ArrayList<User>) in.readObject();
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
    
    private void loadEmails() {
        try {
         FileInputStream fileIn = new FileInputStream("data/emails.ser");
         ObjectInputStream in = new ObjectInputStream(fileIn);
         this.emails = (ArrayList<Email>) in.readObject();
         in.close();
         fileIn.close();
      }catch(IOException i) {
          this.emails = new ArrayList();
      }catch(ClassNotFoundException c) {
          this.emails = new ArrayList();
      }
    }

    @Override
    public void run() {
        
        try {
            OutputStream output = clientSocket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(GetMailRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String subject = "";
        String message = "";
        User userTo = null;
        User userFrom = null;

    }

}
