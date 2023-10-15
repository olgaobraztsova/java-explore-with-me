package ru.practicum.event.model;

import java.util.Optional;

public enum EventStateActionAdmin {
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static Optional<EventStateActionAdmin> from(String stringStateAction) {
        for (EventStateActionAdmin eventStateActionAdmin : values()) {
            if (eventStateActionAdmin.name().equalsIgnoreCase(stringStateAction)) {
                return Optional.of(eventStateActionAdmin);
            }
        }
        return Optional.empty();
    }
}
