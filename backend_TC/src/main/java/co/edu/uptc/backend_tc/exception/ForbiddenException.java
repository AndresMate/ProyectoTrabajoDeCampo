package co.edu.uptc.backend_tc.exception;

public class ForbiddenException extends RuntimeException {

    private final String resource;
    private final String action;

    public ForbiddenException(String message) {
        super(message);
        this.resource = null;
        this.action = null;
    }

    public ForbiddenException(String resource, String action) {
        super(String.format("Access denied: cannot %s %s", action, resource));
        this.resource = resource;
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }
}