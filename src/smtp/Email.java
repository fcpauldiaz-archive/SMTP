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
    
    private String userFrom;
    private String message;
    private String subject;
    private User userTo;
    private String time;
    private String messageFrom;
    private String messageTo;
    private String redirectTo;
    
    public Email(String userFrom, String message, String subject, User userTo, String messageFrom, String messageTo) {
        this.userFrom = userFrom;
        this.message = message;
        this.subject = subject;
        this.userTo = userTo;
        this.messageFrom = messageFrom;
        this.messageTo = messageTo;
        time = new Date().toString();
    }

  

    public Email(User user, String message, String subject) {
        this.userTo = user;
        this.message = message;
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    public User getUserTo() {
        return userTo;
    }

    public void setUserTo(User userTo) {
        this.userTo = userTo;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    public String getTime() {
        return time;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public String getMessageTo() {
        return messageTo;
    }

    public void setMessageTo(String messageTo) {
        this.messageTo = messageTo;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }
    

    @Override
    public String toString() {
        return "Email{" + "userFrom=" + userFrom + ", message=" + message + ", subject=" + subject + ", userTo=" + userTo + '}';
    }

   
    
}
