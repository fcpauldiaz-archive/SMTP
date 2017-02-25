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
            boolean recognized = false;
            OutputStream output = clientSocket.getOutputStream();
            String subject = "";
            String message = "";
            String messageFrom = "";
            String messageTo = "";
            String date = "";
            ArrayList<User> usersTo = new ArrayList();
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
                        
                    if (inputLine.contains("HELO")) {
                        recognized = true;
                       int length = inputLine.length();
                       if (length >= 4) {
                           output.write("250 host at your service\r\n".getBytes());
                           commandValid1 = true;
                       }
                       else
                           output.write("500 missing param at helo\n".getBytes());
                   }
                   if (inputLine.contains("MAILFROM:") && readingData == false) {
                       recognized = true;
                       if (!inputLine.contains("<") || !inputLine.contains(">") 
                           || !inputLine.contains("@") || !inputLine.contains(":")) {
                           output.write("66 Invalid request format\r\n".getBytes());
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
                               output.write("99 email address not valid format\r\n".getBytes());
                           }
                           if (!found && valid) {
                               output.write("99 email address to send not found\r\n".getBytes());
                           }
                           if (found && valid) {
                               output.write("250 2.1.5 OK\r\n".getBytes());
                           }
                           System.out.println(valid);
                           System.out.println(found);

                       }
                   }
                   if (inputLine.contains("RCPTTO:") && readingData == false) {
                       recognized = true;
                       if (!inputLine.contains("<") || !inputLine.contains(">") 
                           || !inputLine.contains("@")) {
                           output.write("66 Invalid email format\r\n".getBytes());
                       }
                       else {
                           String email = this.getEmail(inputLine);
                           //search for valid email adress.
                           boolean found = false;
                           for (User user : this.users) {
                               System.out.println(user.getEmailAdress());
                               if (user.getEmailAdress().toUpperCase().equals(email)){
                                   found = true;
                                   usersTo.add(user);
                               }
                           }
                           if (!found) {
                               output.write("66 email address to send not found\r\n".getBytes());
                           }
                           if (found) {
                               commandValid3 = true;
                               output.write("250 Sender found OK\r\n".getBytes());
                           }

                       }
                   }
                   if (readingData == true) {
                        if (inputLine.contains("SUBJECT:")) {
                           recognized = true;
                           subject = inputLine.substring(msg.indexOf(":")+1, msg.length());
                           System.out.println("Subject saved " + subject);
                        }
                        else if(inputLine.contains("FROM:")) {
                            recognized = true;
                            messageFrom = msg;
                        }
                        else if (inputLine.contains("TO:")) {
                            recognized = true;
                            messageTo += msg + ", ";
                        }
                        else if (inputLine.contains("DATE:")) {
                            recognized = true;
                            date = msg;
                        }
                        else {
                           if (!inputLine.trim().equals(".")) {
                               message += msg + "\r\n";
                               System.out.println("Message saved " + message);
                           }
                           else {
                               readingData = false;
                               commandValid5 = true;
                               output.write("250 OK FINISH\r\n".getBytes());
                           }
                       }
                   }
                   if (inputLine.contains("DATA")) {
                       output.write("354 Go Ahead\r\n".getBytes());
                       readingData = true;
                       commandValid4 = true;
                       recognized = true;
                   }
                   if (inputLine.contains("QUIT")) {
                       recognized = true;
                       if (commandValid1 && commandValid2 
                           && commandValid3 && commandValid4
                           && commandValid5) {
                            
                            for(User user: usersTo) {
                              Email email = new Email(userFrom, message, subject, user, messageFrom, messageTo);
                              if (!date.isEmpty()) {
                                  email.setTime(date);
                              }
                              this.emails.add(email);
                            }
                           
                           this.saveEmails();
                           output.write("221 Saving and closing Connection\r\n".getBytes());
                           output.close();
                       }
                       else {
                           output.write("500 error on the command order\r\n".getBytes());
                           output.close();
                       }
                       break;
                   }
                   if (!(recognized)) {
                       output.write("404 command not recognized\r\n".getBytes());
                   }
                   recognized = false;

                    
                  
                   
                    
                } catch(Exception e){
                    output.write("500 bad request \r\n".getBytes());
                    e.printStackTrace();
                    break;
                }
                        
            }
            output.write("500 connection timed out after 5.5 min\r\n".getBytes());
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
         System.out.println("User saved");
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
         System.out.println("New Email Saved");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
    
    
    public boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
    
    
}