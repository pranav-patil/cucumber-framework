package cukes.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@Component
public class LogbackCapture {

    private final Logger logger;
    private OutputStreamAppender<ILoggingEvent> appender;
    private String capturedLog;
    private static final String DEFAULT_APPENDER = "CAPTURE_LOG";

    public LogbackCapture() {
        this(Level.ERROR, "%logger %msg");
    }

    public LogbackCapture(Level level, String layoutPattern) {

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.logger = context.getLogger(ROOT_LOGGER_NAME);
        logger.setLevel(level);

        Appender<ILoggingEvent> appender = logger.getAppender(DEFAULT_APPENDER);

        if(appender == null) {
            ByteArrayOutputStream logs = new ByteArrayOutputStream(2048);
            Encoder<ILoggingEvent> encoder = buildEncoder(layoutPattern);
            this.appender = buildAppender(DEFAULT_APPENDER, encoder, logs);
            logger.addAppender(this.appender);
        } else if(appender instanceof OutputStreamAppender) {
            this.appender = (OutputStreamAppender<ILoggingEvent>) appender;
        }
    }

    public void startCapture() {
        if(!appender.isStarted()) {
            appender.setOutputStream(new ByteArrayOutputStream(2048));
            appender.start();
        }
    }

    public void stopCapture() {

        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) appender.getOutputStream();
        capturedLog = null;

        if(outputStream != null) {
            try {
                capturedLog = outputStream.toString("UTF-16");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if(appender.isStarted()) {
            appender.stop();
        }
    }

    public String getCapturedLog() {
        return capturedLog;
    }

    private Encoder<ILoggingEvent> buildEncoder(String layoutPattern) {

        if (layoutPattern == null) {
            layoutPattern = "%msg";
        }
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(layoutPattern);
        encoder.setCharset(Charset.forName("UTF-16"));
        encoder.setContext(logger.getLoggerContext());
        encoder.start();
        return encoder;
    }

    private OutputStreamAppender<ILoggingEvent> buildAppender(String appenderName, final Encoder<ILoggingEvent> encoder,
                                                                     final OutputStream outputStream) {
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<ILoggingEvent>();
        appender.setName(appenderName);
        appender.setContext(logger.getLoggerContext());
        appender.setEncoder(encoder);
        appender.setOutputStream(outputStream);
        appender.start();
        return appender;
    }
}