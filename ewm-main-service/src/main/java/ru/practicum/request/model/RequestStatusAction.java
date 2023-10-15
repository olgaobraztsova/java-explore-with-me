package ru.practicum.request.model;


import java.util.Optional;

public enum RequestStatusAction {
    CONFIRMED, REJECTED;

    public static Optional<RequestStatusAction> from(String stringStatusAction) {
        for (RequestStatusAction requestStatusAction : values()) {
            if (requestStatusAction.name().equalsIgnoreCase(stringStatusAction)) {
                return Optional.of(requestStatusAction);
            }
        }
        return Optional.empty();
    }
}