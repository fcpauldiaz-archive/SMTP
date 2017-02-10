/**
 * Universidad Del Valle de Guatemala
 * Pablo Diaz 13203
 * Jan 19, 2017
 **/

package smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author SDX
 */
public class SMTPRequest implements Runnable {

    protected Socket clientSocket = null;
    protected String serverText = null;
    protected WorkingQueue cola = null;

    public SMTPRequest(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
   

    @Override
    public void run() {
        try {
            boolean send = true;
            OutputStream output = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(true) {
                String inputLine = in .readLine();
                System.out.println(inputLine);
            }
            //output.close();
        } catch (IOException ex) {
            System.out.println("Error reading socket");
        }

   }
}