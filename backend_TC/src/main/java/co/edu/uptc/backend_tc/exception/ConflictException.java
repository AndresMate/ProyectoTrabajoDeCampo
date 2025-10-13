package co.edu.uptc.backend_tc.exception;

public class ConflictException extends RuntimeException {

    private final String conflictField;
    private final Object conflictValue;

    public ConflictException(String message) {
        super(message);
        this.conflictField = null;
        this.conflictValue = null;
    }

    public ConflictException(String message, String conflictField, Object conflictValue) {
        super(message);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public String getConflictField() {
        return conflictField;
    }

    public Object getConflictValue() {
        return conflictValue;
    }
}