package Logger;

public interface LogSink {
    void logMessage(LogMessage message);

    LogLevel getMinLogLevel();

    void setMinLogLevel(LogLevel level);
}
