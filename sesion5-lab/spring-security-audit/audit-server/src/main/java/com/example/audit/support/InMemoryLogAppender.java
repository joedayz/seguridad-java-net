package com.example.audit.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/** Captura lineas de log para mostrarlas en la respuesta HTTP de la demo. */
public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static final List<String> LINES = Collections.synchronizedList(new ArrayList<>());

    public static void clear() {
        LINES.clear();
    }

    public static List<String> snapshot() {
        return List.copyOf(LINES);
    }

    @Override
    protected void append(ILoggingEvent event) {
        LINES.add(event.getFormattedMessage());
    }
}
