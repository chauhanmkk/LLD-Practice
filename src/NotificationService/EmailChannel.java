package NotificationService;

public class EmailChannel implements Channel {
    @Override
    public void notify(User user, Message message) {
        System.out.println("Email : Notifying to user "+ user.userName + "with message "+message.message);
    }
}
