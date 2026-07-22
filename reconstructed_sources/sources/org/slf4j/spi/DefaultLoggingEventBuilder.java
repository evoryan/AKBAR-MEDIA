package org.slf4j.spi;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.DefaultLoggingEvent;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/* loaded from: classes12.dex */
public class DefaultLoggingEventBuilder implements LoggingEventBuilder, CallerBoundaryAware {
    static String DLEB_FQCN = DefaultLoggingEventBuilder.class.getName();
    protected Logger logger;
    protected DefaultLoggingEvent loggingEvent;

    public DefaultLoggingEventBuilder(Logger logger, Level level) {
        this.logger = logger;
        this.loggingEvent = new DefaultLoggingEvent(level, logger);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder addMarker(Marker marker) {
        this.loggingEvent.addMarker(marker);
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder setCause(Throwable t) {
        this.loggingEvent.setThrowable(t);
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder addArgument(Object p) {
        this.loggingEvent.addArgument(p);
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder addArgument(Supplier<?> objectSupplier) {
        this.loggingEvent.addArgument(objectSupplier.get());
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder addKeyValue(String key, Object value) {
        this.loggingEvent.addKeyValue(key, value);
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder addKeyValue(String key, Supplier<Object> value) {
        this.loggingEvent.addKeyValue(key, value.get());
        return this;
    }

    @Override // org.slf4j.spi.CallerBoundaryAware
    public void setCallerBoundary(String fqcn) {
        this.loggingEvent.setCallerBoundary(fqcn);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public void log() {
        log(this.loggingEvent);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder setMessage(String message) {
        this.loggingEvent.setMessage(message);
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public LoggingEventBuilder setMessage(Supplier<String> messageSupplier) {
        this.loggingEvent.setMessage(messageSupplier.get());
        return this;
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public void log(String message) {
        this.loggingEvent.setMessage(message);
        log(this.loggingEvent);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public void log(String message, Object arg) {
        this.loggingEvent.setMessage(message);
        this.loggingEvent.addArgument(arg);
        log(this.loggingEvent);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public void log(String message, Object arg0, Object arg1) {
        this.loggingEvent.setMessage(message);
        this.loggingEvent.addArgument(arg0);
        this.loggingEvent.addArgument(arg1);
        log(this.loggingEvent);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public void log(String message, Object... args) {
        this.loggingEvent.setMessage(message);
        this.loggingEvent.addArguments(args);
        log(this.loggingEvent);
    }

    @Override // org.slf4j.spi.LoggingEventBuilder
    public void log(Supplier<String> messageSupplier) {
        if (messageSupplier == null) {
            log((String) null);
        } else {
            log(messageSupplier.get());
        }
    }

    protected void log(LoggingEvent aLoggingEvent) {
        if (aLoggingEvent.getCallerBoundary() == null) {
            setCallerBoundary(DLEB_FQCN);
        }
        boolean z = this.logger instanceof LoggingEventAware;
        Logger logger = this.logger;
        if (z) {
            ((LoggingEventAware) logger).log(aLoggingEvent);
        } else if (logger instanceof LocationAwareLogger) {
            logViaLocationAwareLoggerAPI((LocationAwareLogger) this.logger, aLoggingEvent);
        } else {
            logViaPublicSLF4JLoggerAPI(aLoggingEvent);
        }
    }

    private void logViaLocationAwareLoggerAPI(LocationAwareLogger locationAwareLogger, LoggingEvent aLoggingEvent) {
        aLoggingEvent.getMessage();
        aLoggingEvent.getMarkers();
        String mergedMessage = mergeMarkersAndKeyValuePairsAndMessage(aLoggingEvent);
        locationAwareLogger.log(null, aLoggingEvent.getCallerBoundary(), aLoggingEvent.getLevel().toInt(), mergedMessage, aLoggingEvent.getArgumentArray(), aLoggingEvent.getThrowable());
    }

    private void logViaPublicSLF4JLoggerAPI(LoggingEvent aLoggingEvent) {
        Object[] argArray = aLoggingEvent.getArgumentArray();
        int argLen = argArray == null ? 0 : argArray.length;
        Throwable t = aLoggingEvent.getThrowable();
        int tLen = t == null ? 0 : 1;
        Object[] combinedArguments = new Object[argLen + tLen];
        if (argArray != null) {
            System.arraycopy(argArray, 0, combinedArguments, 0, argLen);
        }
        if (t != null) {
            combinedArguments[argLen] = t;
        }
        String mergedMessage = mergeMarkersAndKeyValuePairsAndMessage(aLoggingEvent);
        switch (aLoggingEvent.getLevel()) {
            case TRACE:
                this.logger.trace(mergedMessage, combinedArguments);
                return;
            case DEBUG:
                this.logger.debug(mergedMessage, combinedArguments);
                return;
            case INFO:
                this.logger.info(mergedMessage, combinedArguments);
                return;
            case WARN:
                this.logger.warn(mergedMessage, combinedArguments);
                return;
            case ERROR:
                this.logger.error(mergedMessage, combinedArguments);
                return;
            default:
                return;
        }
    }

    private String mergeMarkersAndKeyValuePairsAndMessage(LoggingEvent aLoggingEvent) {
        StringBuilder sb = mergeMarkers(aLoggingEvent.getMarkers(), null);
        String mergedMessage = mergeMessage(aLoggingEvent.getMessage(), mergeKeyValuePairs(aLoggingEvent.getKeyValuePairs(), sb));
        return mergedMessage;
    }

    private StringBuilder mergeMarkers(List<Marker> markerList, StringBuilder sb) {
        if (markerList == null || markerList.isEmpty()) {
            return sb;
        }
        if (sb == null) {
            sb = new StringBuilder();
        }
        for (Marker marker : markerList) {
            sb.append(marker);
            sb.append(' ');
        }
        return sb;
    }

    private StringBuilder mergeKeyValuePairs(List<KeyValuePair> keyValuePairList, StringBuilder sb) {
        if (keyValuePairList == null || keyValuePairList.isEmpty()) {
            return sb;
        }
        if (sb == null) {
            sb = new StringBuilder();
        }
        for (KeyValuePair kvp : keyValuePairList) {
            sb.append(kvp.key);
            sb.append('=');
            sb.append(kvp.value);
            sb.append(' ');
        }
        return sb;
    }

    private String mergeMessage(String msg, StringBuilder sb) {
        if (sb != null) {
            sb.append(msg);
            return sb.toString();
        }
        return msg;
    }
}
