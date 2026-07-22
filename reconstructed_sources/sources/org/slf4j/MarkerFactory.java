package org.slf4j;

import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.Reporter;
import org.slf4j.spi.SLF4JServiceProvider;

/* loaded from: classes12.dex */
public class MarkerFactory {
    static IMarkerFactory MARKER_FACTORY;

    private MarkerFactory() {
    }

    static {
        SLF4JServiceProvider provider = LoggerFactory.getProvider();
        if (provider != null) {
            MARKER_FACTORY = provider.getMarkerFactory();
            return;
        }
        Reporter.error("Failed to find provider");
        Reporter.error("Defaulting to BasicMarkerFactory.");
        MARKER_FACTORY = new BasicMarkerFactory();
    }

    public static Marker getMarker(String name) {
        return MARKER_FACTORY.getMarker(name);
    }

    public static Marker getDetachedMarker(String name) {
        return MARKER_FACTORY.getDetachedMarker(name);
    }

    public static IMarkerFactory getIMarkerFactory() {
        return MARKER_FACTORY;
    }
}
