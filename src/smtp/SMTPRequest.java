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

    public SMTPRequest(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.emails = new ArrayList();
        this.users = new ArrayList();
    }
   

    @Override
    public void run() {
        try {
            String inputArray[];
            boolean readingData = false;
            OutputStream output = clientSocket.getOutputStream();
            String subject = "";
            String message = "";
            User userTo = null;
            User userFrom = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            long t = System.currentTimeMillis();
            t = t + 90*1000; //1.5 minutes time out
            while(System.currentTimeMillis() < t) {
                try {
                    String inputLine = in .readLine();
                    String msg = inputLine;
                    inputLine = inputLine.toUpperCase();
                    System.out.println(inputLine);
                    if (inputLine.contains("HELO")){
                        inputArray = inputLine.split(" ");
                        if (inputArray.length >= 2) 
                            output.write("250 host at your service\n".getBytes());
                        else
                            output.write("500 missing param at helo\n".getBytes());
                    }
                    if (inputLine.contains("MAIL FROM:")) {
                        if (!inputLine.contains("<") || !inputLine.contains(">") 
                            || !inputLine.contains("@")) {
                            output.write("500 Invalid email format\n".getBytes());
                        }
                        else {
                            String email = this.getEmail(inputLine);
                            //search for valid email adress.
                            boolean found = false;
                            for (User user : this.users) {
                                if (user.getEmailAdress().equals(email)){
                                    found = true;
                                    userFrom = user;
                                }
                            }
                            if (!found) {
                                output.write("500 email address to send not found\n".getBytes());
                            }
                            if (found) {
                                output.write("250 2.1.5 OK\n".getBytes());
                            }
                            
                        }
                    }
                    if (inputLine.contains("RCPT TO:")) {
                        if (!inputLine.contains("<") || !inputLine.contains(">") 
                            || !inputLine.contains("@")) {
                            output.write("500 Invalid email format\n".getBytes());
                        }
                        else {
                            String email = this.getEmail(inputLine);
                            //search for valid email adress.
                            boolean found = false;
                            for (User user : this.users) {
                                if (user.getEmailAdress().equals(email)){
                                    found = true;
                                    userTo = user;
                                }
                            }
                            if (!found) {
                                output.write("500 email address to send not found\n".getBytes());
                            }
                            if (found) {
                                output.write("250 2.1.5 OK\n".getBytes());
                            }
                            
                        }
                    }
                    if (inputLine.contains("DATA")) {
                        output.write("354 Go Ahead".getBytes());
                        readingData = true;
                    }
                    if (readingData == true) {
                        if (inputLine.contains("SUBJECT:")) {
                            subject = inputLine.substring(8, inputLine.length());
                        }
                        else {
                            if (!inputLine.trim().equals(".")) {
                                message += msg;
                            }
                            else {
                                readingData = false;
                                output.write("250 OK FINISH\n".getBytes());
                            }
                        }
                    }
                    if (inputLine.contains("QUIT")) {
                        Email email = new Email(userFrom, subject, message, userTo);
                        this.emails.add(email);
                        this.saveEmails();
                        output.write("Closing Connection\n".getBytes());
                        output.close();
                        break;
                    }
                    
                    
                } catch(Exception e){
                        break;
                }
                        
            }
            output.write("500 connection timed out after 1.5 min\n".getBytes());
            output.close();
        } catch (IOException ex) {
            System.out.println("Error reading socket");
        }

   }
    
    public String getEmail(String inputLine) {
        inputLine = inputLine.substring(1, inputLine.length()-1);
        return inputLine;
    }
    
    public String getHost(String inputLine) {
        inputLine = inputLine.substring(inputLine.indexOf("@"), inputLine.length());
        return inputLine;
    }
    
    
    public void loadUsers() {
        try {
         FileInputStream fileIn = new FileInputStream("/data/users.ser");
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
    
    public void loadEmails() {
        try {
         FileInputStream fileIn = new FileInputStream("/data/emails.ser");
         ObjectInputStream in = new ObjectInputStream(fileIn);
         this.emails = (ArrayList<Email>) in.readObject();
         in.close();
         fileIn.close();
      }catch(IOException i) {
         i.printStackTrace();
         return;
      }catch(ClassNotFoundException c) {
         System.out.println("Emails class not found");
         c.printStackTrace();
         return;
      }
    }
    
    public void saveUsers() {
        try {
         FileOutputStream fileOut =
         new FileOutputStream("/data/users.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.users);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in /tmp/employee.ser");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
    
    public void saveEmails() {
         try {
         FileOutputStream fileOut =
         new FileOutputStream("/data/emails.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(this.emails);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in /tmp/employee.ser");
      }catch(IOException i) {
         i.printStackTrace();
      }
    }
    
    
}