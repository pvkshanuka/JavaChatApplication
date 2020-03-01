import java.util.Date;

public class RecivedMessage {

    private String senderAddress;
    private String senderName;
    private Date date;
    private String message;

    public RecivedMessage(String senderAddress, String senderName, Date date, String message) {
        this.senderAddress = senderAddress;
        this.senderName = senderName;
        this.date = date;
        this.message = message;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
