package ru.practicum.event.model;

import java.util.Optional;

public enum EventStateActionUser {
    SEND_TO_REVIEW,
    CANCEL_REVIEW;

    public static Optional<EventStateActionUser> from(String stringStateAction) {
        for (EventStateActionUser eventStateActionUser : values()) {
            if (eventStateActionUser.name().equalsIgnoreCase(stringStateAction)) {
                return Optional.of(eventStateActionUser);
            }
        }
        return Optional.empty();
    }
}
