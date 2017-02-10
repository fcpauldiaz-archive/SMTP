/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Jan 26, 2017
**/

package smtp;

/**
 * clase que crea la cantidad de threads
 * @author SDX
 */
public class Pool {

    WorkingQueue queue;
    
    public Pool(int maxThread) {
        //la cantidad maxima de threads que pueden haber
        this.queue = new WorkingQueue(maxThread);
        //crear la cantidad de threads definidos en el pool
        for (int i = 0; i < maxThread; i++) {
            String threadName = "Thread - " + i;
            Request request = new Request(queue);
            Thread thread = new Thread(request, threadName);
            thread.start();
        }
    }

    public void addRequest(SMTPRequest rq) throws InterruptedException  {
        queue.put(rq);
    }
}
