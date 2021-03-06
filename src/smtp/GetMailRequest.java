/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 16, 2017
**/

package smtp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public final Pattern VALID_EMAIL_ADDRESS_REGEX = 
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    
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
          this.users = new ArrayList();
         return;
      }catch(ClassNotFoundException c) {
          this.users = new ArrayList();
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
            boolean emailFound = false;
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
                            emailFound = true;
                            output.write("200 email found\r\n".getBytes());
                            sendEmailData(output, user);
                            output.close();
                        }
                    }
                }
                if (inputLine.contains("CREATE:")) {
                    inputLine = inputLine.substring(inputLine.indexOf(":")+1, inputLine.length());
                    emailValid = this.validate(inputLine.toLowerCase());
                    for (int i = 0;  i < this.users.size(); i++) {
                       //  System.out.println(this.users.get(i).getEmailAdress());
                        if (this.users.get(i).getEmailAdress().equals(inputLine.toLowerCase())){
                            emailValid = false;
                        }
                    }
                    if (emailValid == true) {
                        this.users.add(new User(inputLine.toLowerCase()));
                        this.saveUsers();
                        output.write("200\r\n".getBytes());
                        emailFound = true;
                    } 
                    
                }
                if (emailValid == false) {
                    output.write("66 Email not valid\r\n".getBytes());
                }
                if (emailFound == false) {
                    output.write("99 Email not found\r\n".getBytes());
                }
                emailValid = false;
                emailFound = false;
            }
        } catch (IOException ex) {
            System.out.println("Error socket");
        }
    }
    
    public void sendEmailData(OutputStream output, User user) throws IOException {
        ArrayList<Integer> deleteIndex = new ArrayList();
        for (int j = 0; j < this.emails.size(); j++) {
            Email email = this.emails.get(j);

            if (email.getUserTo().getEmailAdress().equals(user.getEmailAdress())) {
                deleteIndex.add(j);
                try {
                    output.write("START \r\n".getBytes());
                    output.write(("FROM: " + email.getUserFrom() + "\r\n").getBytes());
                    output.write(("SUBJECT: " + email.getSubject()+ "\r\n").getBytes() );
                    output.write(("DATA: " + email.getMessage()+ "\r\n").getBytes());
                    output.write(("DATE: " + email.getTime()+ "\r\n").getBytes());
                    output.write("FINISH\r\n".getBytes());
                } catch(Exception e){
                    System.out.println("Error sending to user agent");
                }
            }
        }
        //POP3
        //delete messages from server
        //for (Integer deleteIndex1 : deleteIndex) {
        //    this.emails.remove((int) deleteIndex1);
        //}
        // this.saveEmails();
        output.write("200 emails sent\r\n".getBytes());
    }
    
     
    public void saveEmails() {
        try {
         FileOutputStream fileOut =
         new FileOutputStream("data/emails.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.emails);
         out.close();
         fileOut.close();
         System.out.println("New Email Saved");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
    
    
    public void saveUsers() {
        try {
         FileOutputStream fileOut =
         new FileOutputStream("data/users.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.users);
         out.close();
         fileOut.close();
         System.out.println("User saved");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
      
    public boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}
