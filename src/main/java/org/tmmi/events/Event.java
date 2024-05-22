package org.tmmi.events;

import org.jetbrains.annotations.NotNull;

public abstract class Event {
    private final String name;

    public Event() {
        this.name = this.getClass().getSimpleName();
    }

    @NotNull
    public String getEventName() {
        return this.name;
    }

}
