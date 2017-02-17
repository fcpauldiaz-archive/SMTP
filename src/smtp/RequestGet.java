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

public class RequestGet implements Runnable {
    
    private GetMailQueue colaMail;

    public RequestGet(GetMailQueue queue) {
        this.colaMail = queue;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                String name = Thread.currentThread().getName();
                Runnable request = colaMail.get();
                System.out.println("Request empieza por : " + name);
                request.run();
                System.out.println("Request termina por : " + name);
            }
        } catch (InterruptedException e) {

        }
    }

}
