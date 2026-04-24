package Logger;

import java.util.List;

public class Logger {
    List<LogSink> sinks;

    Logger(List<LogSink> sinks) {
        this.sinks = sinks;
    }

    void info(String message) {
        LogMessage logMessage = new LogMessage(1,LogLevel.INFO, message);
        for(LogSink sink : sinks) {
            sink.logMessage(logMessage);
        }
    }
}
