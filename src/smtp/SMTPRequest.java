/**
 * Universidad Del Valle de Guatemala
 * Pablo Diaz 13203
 * Jan 19, 2017
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
public class SMTPRequest implements Runnable {

    protected Socket clientSocket = null;
    protected String serverText = null;
    protected WorkingQueue cola = null;
    protected ArrayList<Email> emails;
    protected ArrayList<User> users;
    public final Pattern VALID_EMAIL_ADDRESS_REGEX = 
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    public SMTPRequest(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.loadUsers();
        this.loadEmails();
        System.out.println(emails);
    }
   

    @Override
    public void run() {
        try {
            
            boolean readingData = false;
            boolean commandValid1 = false;
            boolean commandValid2 = false;
            boolean commandValid3 = false;
            boolean commandValid4 = false;
            boolean commandValid5 = false;
            OutputStream output = clientSocket.getOutputStream();
            String subject = "";
            String message = "";
            User userTo = null;
            User userFrom = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            long t = System.currentTimeMillis();
            t = t + 300*1000; //5 minutes time out
            while(System.currentTimeMillis() < t) {
                try {
                    String inputLine = in.readLine().replaceAll("\\s+","");
                    String msg = inputLine;
                    inputLine = inputLine.toUpperCase().trim();
                    System.out.println(inputLine);
                    if (inputLine.contains("HELO")){
                        int length = inputLine.length();
                        if (length >= 4) {
                            output.write("250 host at your service\n".getBytes());
                            commandValid1 = true;
                        }
                        else
                            output.write("500 missing param at helo\n".getBytes());
                    }
                    if (inputLine.contains("MAILFROM")) {
                        if (!inputLine.contains("<") || !inputLine.contains(">") 
                            || !inputLine.contains("@") || !inputLine.contains(":")) {
                            output.write("500 Invalid request format\n".getBytes());
                        }
                        else {
                            String email = this.getEmail(inputLine);
                            System.out.println(email);
                            boolean valid = this.validate(email);
                            //search for valid email adress.
                            boolean found = false;
                            if (valid) {
                          
                                for (User user : this.users) {
                                    System.out.println(user.getEmailAdress());
                                    System.out.println(email.toUpperCase());
                                    if (user.getEmailAdress().toUpperCase().equals(email)){
                                        found = true;
                                        userFrom = user;
                                        commandValid2 = true;
                                    }
                                }
                            } else {
                                output.write("545 email address not valid format\n".getBytes());
                            }
                            if (!found && valid) {
                                output.write("550 email address to send not found\n".getBytes());
                            }
                            if (found && valid) {
                                output.write("250 2.1.5 OK\n".getBytes());
                            }
                            System.out.println(valid);
                            System.out.println(found);
                            
                        }
                    }
                    if (inputLine.contains("RCPTTO")) {
                        if (!inputLine.contains("<") || !inputLine.contains(">") 
                            || !inputLine.contains("@")) {
                            output.write("500 Invalid email format\n".getBytes());
                        }
                        else {
                            String email = this.getEmail(inputLine);
                            //search for valid email adress.
                            boolean found = false;
                            for (User user : this.users) {
                                System.out.println(user.getEmailAdress());
                                if (user.getEmailAdress().toUpperCase().equals(email)){
                                    found = true;
                                    userTo = user;
                                }
                            }
                            if (!found) {
                                output.write("500 email address to send not found\n".getBytes());
                            }
                            if (found) {
                                commandValid3 = true;
                                output.write("250 Sender found OK\n".getBytes());
                            }
                            
                        }
                    }
                    if (readingData == true) {
                        if (inputLine.contains("SUBJECT:")) {
                            subject = inputLine.substring(msg.indexOf(":")+1, msg.length());
                            System.out.println("Subject saved " + subject);
                        }
                        else {
                            if (!inputLine.trim().equals(".")) {
                                message += msg + "\n";
                                System.out.println("Message saved " + message);
                            }
                            else {
                                readingData = false;
                                commandValid5 = true;
                                output.write("250 OK FINISH\n".getBytes());
                            }
                        }
                    }
                    if (inputLine.contains("DATA")) {
                        output.write("354 Go Ahead\n".getBytes());
                        readingData = true;
                        commandValid4 = true;
                    }
                    if (inputLine.contains("QUIT")) {
                        if (commandValid1 && commandValid2 
                            && commandValid3 && commandValid4
                            && commandValid5) {
                            Email email = new Email(userFrom, message, subject, userTo);
                            this.emails.add(email);
                            this.saveEmails();
                            output.write("Saving and closing Connection\n".getBytes());
                            output.close();
                        }
                        else {
                            output.write("500 error on the command order\n".getBytes());
                        }
                        break;
                    }
                    if (!(commandValid1 || commandValid2 
                            || commandValid3 || commandValid4
                            || commandValid5)) {
                        output.write("404 command not recognized\n".getBytes());
                    }
                    
                } catch(Exception e){
                    output.write("500 bad request \n".getBytes());
                    e.printStackTrace();
                    break;
                }
                        
            }
            output.write("500 connection timed out after 5.5 min\n".getBytes());
            output.close();
        } catch (IOException ex) {
            System.out.println("Error reading socket");
        }

   }
    
    public String getEmail(String inputLine) {
        inputLine = inputLine.substring(inputLine.indexOf("<")+1, inputLine.length()-1);
        return inputLine;
    }
    
    public String getHost(String inputLine) {
        inputLine = inputLine.substring(inputLine.indexOf("@"), inputLine.length());
        return inputLine;
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
    
    public void saveUsers() {
        try {
         FileOutputStream fileOut =
         new FileOutputStream("data/users.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.users);
         out.close();
         fileOut.close();
         System.out.println("Serialized data is saved in /tmp/employee.ser");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
    
    public void saveEmails() {
        try {
         FileOutputStream fileOut =
         new FileOutputStream("data/emails.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.emails);
         out.close();
         fileOut.close();
         System.out.println("Serialized data is saved in /tmp/employee.ser");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
    
    
    public boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
    
    
}