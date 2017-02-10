/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Jan 19, 2017
**/

package smtp;


import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadedServer implements Runnable{

    protected int puerto = 8080;
    protected ServerSocket serverSocket;
    protected boolean finished    = false;
    protected Thread currentThread;
    protected Pool cola;

    public MultiThreadedServer(int port, int size ) {
        this.puerto = port;
        this.cola = new Pool(size);
    }

    @Override
    public synchronized void run() {
        
        openServerSocket();
        while(! finished()){
            
                Socket clientSocket = null;
                try {
                    clientSocket = this.serverSocket.accept();
                } catch (IOException e) {
                    if(finished()) {
                        System.out.println("Server Stopped.");
                        return;
                    }
                    System.out.println(e);
                }
                
            try {
                /*op = new Thread(
                new SMTPRequest(
                clientSocket, "localhost")
                );*/
                //solo agregue esta linea
                cola.addRequest(new SMTPRequest(clientSocket));
            } catch (InterruptedException ex) {
                Logger.getLogger(MultiThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean finished() {
        return this.finished;
    }

    public synchronized void stop(){
        this.finished = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.puerto);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
