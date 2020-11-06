package util;

public class LoneOrganizerException extends RuntimeException {
    public LoneOrganizerException() {
        super("There must be at least one organizer for a conference.");
    }
}