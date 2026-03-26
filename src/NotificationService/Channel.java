package NotificationService;

public interface Channel {
    void notify(User user, Message message);
}
