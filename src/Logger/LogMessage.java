package Logger;

public class LogMessage {
    int messageId;
    LogLevel level;
    String message;

    public LogMessage(int messageId, LogLevel level, String message) {
        this.messageId = messageId;
        this.level = level;
        this.message = message;
    }
}
