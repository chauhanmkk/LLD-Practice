package Logger;

public class ConsoleSink implements LogSink {
    private LogLevel minLogLevel = LogLevel.INFO;
    @Override
    public void logMessage(LogMessage message) {
        if(message.level.getLevel() >= minLogLevel.getLevel()) {
            System.out.println(message.message);
        }
    }

    @Override
    public LogLevel getMinLogLevel() {
        return this.minLogLevel;
    }

    @Override
    public void setMinLogLevel(LogLevel level) {
        this.minLogLevel = level;
    }
}
