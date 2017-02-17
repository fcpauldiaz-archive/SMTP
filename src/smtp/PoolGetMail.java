/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 16, 2017
**/

package smtp;

/**
 *
 * @author SDX
 */
public class PoolGetMail {
    private GetMailQueue queue;
    
    public PoolGetMail(int maxThread) {
    //la cantidad maxima de threads que pueden haber
        this.queue = new GetMailQueue(maxThread);
        //crear la cantidad de threads definidos en el pool
        for (int i = 0; i < maxThread; i++) {
            String threadName = "Thread - " + i;
            RequestGet request = new RequestGet(queue);
            Thread thread = new Thread(request, threadName);
            thread.start();
        }
    }

    public void addRequest(GetMailRequest rq) throws InterruptedException  {
        queue.put(rq);
    }
}
