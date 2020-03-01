import java.util.ArrayList;
import java.util.Map;

public class ClientChat {

    private String address;
    private String name;
    private ArrayList<RecivedMessage> recivedMessages = new ArrayList<>();

    public ClientChat(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<RecivedMessage> getRecivedMessages() {
        return recivedMessages;
    }

    public void addRecivedMessages(RecivedMessage recivedMessage) {
        this.recivedMessages.add(recivedMessage);
    }
}
