/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 16, 2017
**/

package smtp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
        boolean emailValid = false;
        String subject = "";
        String message = "";
        User user = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while(true) {
            String inputLine = in.readLine().replaceAll("\\s+","");
            String msg = inputLine;
            inputLine = inputLine.toUpperCase().trim();
            System.out.println(inputLine);
            if (inputLine.contains("EMAIL:")) {
                inputLine = inputLine.substring(inputLine.indexOf(":")+1, inputLine.length());
                System.out.println(inputLine);
                for (int i = 0; i < this.users.size(); i++) {
                    if (this.users.get(i).getEmailAdress().equals(inputLine.toLowerCase())) {
                        user = this.users.get(i);
                        emailValid = true;
                        output.write("200 email found\r\n".getBytes());
                        sendEmailData(output, user);
                    }
                }
            }
            if (emailValid == false) {
                output.write("Email not found\r\n".getBytes());
            }
        }
        } catch (IOException ex) {
            System.out.println("Error socket");
        }
    }
    
    public void sendEmailData(OutputStream output, User user) {
        for (int i = 0; i < this.emails.size(); i++){
            Email email = this.emails.get(i);
            if (email.getUserTo().getEmailAdress().equals(user.getEmailAdress())) {
                try {
                    output.write("START \r\n".getBytes());
                    output.write(("FROM: " + email.getUserFrom().getEmailAdress() + "\r\n").getBytes());
                    output.write(("SUBJECT: " + email.getSubject()+ "\r\n").getBytes() );
                    output.write(("DATA: " + email.getMessage()+ "\r\n").getBytes());
                    output.write(("DATE: " + email.getTime()+ "\r\n").getBytes());
                    output.write("FINISH\r\n".getBytes());
                } catch(Exception e){
                    System.out.println("Error sending to user agent");
                }
            }
        }
    }

}
