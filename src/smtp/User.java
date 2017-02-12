/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 11, 2017
**/

package smtp;

/**
 *
 * @author SDX
 */
public class User {

    private String emailAdress;

    public User(String emailAdress) {
        this.emailAdress = emailAdress;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public void setEmailAdress(String emailAdress) {
        this.emailAdress = emailAdress;
    }

    @Override
    public String toString() {
        return "User{" + "emailAdress=" + emailAdress + '}';
    }
    
    
}
