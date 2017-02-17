/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 16, 2017
**/

package smtp;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author SDX
 */
public class GetMailQueue {
    private Queue cola;
    private int maxSize;
    
    public GetMailQueue(int maxSize) {
        this.cola = new LinkedList();
        this.maxSize = maxSize;
    }
    
    public synchronized GetMailRequest get() throws InterruptedException {
        while(this.cola.size() == 0) {
            //creo que aqui iria el modo pánico
            //pero no puedo instanciar threads aqui ¿?
            wait();
        }
        if(this.cola.size() == maxSize) {
            notifyAll();
        }
        return (GetMailRequest)this.cola.poll();
    }
    
    public synchronized void put(GetMailRequest e) throws InterruptedException {
        while(this.cola.size() == maxSize) {
            wait();
        }
        if (this.cola.isEmpty()) {
            notifyAll();
        }
        this.cola.add(e);
    }
    
}
