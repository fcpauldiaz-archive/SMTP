/**
 * Universidad Del Valle de Guatemala
 * Pablo Diaz 13203
 * Jan 19, 2017
 **/

package smtp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            String inputLine = in .readLine();
            String dir = inputLine.substring(inputLine.indexOf("/"), inputLine.indexOf("HTTP"));
            int contentLength = 0;
            String archivo = "";
            String contentType = "";
            //Thread.currentThread().setName(dir.trim());
            if (dir.trim().equals("/")) {
                //render default file
                File file = new File("index.html");
                contentType = "text/html";

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String text = null;

                    while ((text = reader.readLine()) != null) {
                        archivo += text;
                    }
                    contentLength = archivo.length();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (dir.trim().length() != 0 && !dir.trim().equals("\\")) {
                contentType = dir.substring(dir.indexOf(".") + 1).trim();
                File file = new File(dir.substring(1).trim());

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String text = null;
                    //check for images
                    if (contentType.contains("png") ||
                        contentType.contains("jpg") ||
                        contentType.contains("bmp") ||
                        contentType.contains("jpeg")) {

                        //send image to client
                        sendImage(file, contentType, output);
                        send = false;
                    } else {
                        //if not image append text
                        while ((text = reader.readLine()) != null) {
                            archivo += text;
                        }
                    }

                    contentLength = archivo.length();

                    if (contentType.equals("js")) {
                        contentType = "javascript";
                    }
                    contentType = "text/" + contentType;
                    

                    System.out.println(contentType);
                //if the file is not found
                } catch (FileNotFoundException e) {
                    System.out.println("Archivo no encontrado");
                    //404
                    file = new File("404.html");
                    contentType = "text/html";
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String text = null;

                        while ((text = reader.readLine()) != null) {
                            archivo += text;
                        }

                        contentLength = archivo.length();

                    } catch (FileNotFoundException e2) {}
                }//cierra catch file not found
            }//cierra if not root

            long time = System.currentTimeMillis();
            //if not image was sent
            if (send) {
                output.write(
                    ("HTTP/1.1 200 OK \n" +
                        "Connection close\n" +
                        "Date: Thu, 06 Aug 1998 12:00:15 GMT \n" +
                        "Server: fcpauldiaz\n" +
                        "Last-Modified: Mon, 22 Jun 1998 …... \n" +
                        "Content-Length: " + contentLength + " \n" +
                        "Content-Type: " + contentType +
                        "\n\n" +
                        archivo

                    ).getBytes());
                output.write("".getBytes());
            }
            output.close();
            try {
                // in.close();
                // Sleep a proposito para evaluar el pool
                Thread.sleep(5 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SMTPRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Request processed: " + time);
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }

    /**
     * Método para enviar una imagen a través de un socket
     * @param file
     * @param contentType
     * @param output
     * @throws IOException 
     */
    private void sendImage(File file, String contentType, OutputStream output) throws IOException {
        byte[] bFile = Files.readAllBytes(file.toPath());
        int contentLength = bFile.length;

        byte[] a = ("HTTP/1.1 200 OK \n" +
            "Connection close\n" +
            "Date: Thu, 06 Aug 1998 12:00:15 GMT \n" +
            "Server: fcpauldiaz\n" +
            "Last-Modified: Mon, 22 Jun 1998 …... \n" +
            "Content-Length: " + contentLength + " \n" +
            "Content-Type: " + "image/" + contentType +
            "\n\n"
        ).getBytes();
        byte[] imageBuffer = new byte[a.length + bFile.length];
        System.arraycopy(a, 0, imageBuffer, 0, a.length);
        System.arraycopy(bFile, 0, imageBuffer, a.length, bFile.length);
        output.write(imageBuffer);
    }
}