/**
* Universidad Del Valle de Guatemala
* Pablo Diaz 13203
* Feb 11, 2017
**/

package smtp;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author SDX
 */
public class Email implements Serializable {
    
    private User userFrom;
    private String message;
    private String subject;
    private User userTo;
    private String time;
    
    public Email(User userFrom, String message, String subject, User userTo) {
        this.userFrom = userFrom;
        this.message = message;
        this.subject = subject;
        this.userTo = userTo;
        time = new Date().toString();
    }

  

    public Email(User user, String message, String subject) {
        this.userFrom = user;
        this.message = message;
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public User getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(User userFrom) {
        this.userFrom = userFrom;
    }

    public User getUserTo() {
        return userTo;
    }

    public void setUserTo(User userTo) {
        this.userTo = userTo;
    }
    
    
   
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Email{" + "userFrom=" + userFrom + ", message=" + message + ", subject=" + subject + ", userTo=" + userTo + '}';
    }

   
    
}
