package NotificationService;


public class Message {
    String message;
    MessageType messageType;

    public Message(String message, MessageType messageType) {
        this.message = message;
        this.messageType = messageType;
    }
}
