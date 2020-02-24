import java.util.ArrayList;

public class ChatMessages {
    ArrayList<ChatMessage> messages = new ArrayList<>();

    public synchronized ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    public void addMessage(String message){
        messages.add(new ChatMessage(message,false));
    }
}
