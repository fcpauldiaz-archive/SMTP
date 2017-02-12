/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 11, 2017
**/

package smtp;

import java.io.Serializable;

/**
 *
 * @author SDX
 */
public class Email implements Serializable {
    
    private User user;
    private String message;

    public Email(User user, String message) {
        this.user = user;
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Email{" + "user=" + user + ", message=" + message + '}';
    }
    
    
}
