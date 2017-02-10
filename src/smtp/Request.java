/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Jan 26, 2017
**/

package smtp;

/**
 * Esta clase solo sirve para llamar al metodo run de los runnable y prints
 * @author SDX
 */
public class Request implements Runnable {
    
    private WorkingQueue cola;
    public Request(WorkingQueue queue) {
        this.cola = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String name = Thread.currentThread().getName();
                Runnable request = cola.get();
                System.out.println("Request empieza por : " + name);
                request.run();
                System.out.println("Request termina por : " + name);
            }
        } catch (InterruptedException e) {

        }
    }

}
