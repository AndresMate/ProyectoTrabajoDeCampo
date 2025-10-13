package co.edu.uptc.backend_tc.exception;

public class BadRequestException extends RuntimeException {

    private final String detail;

    public BadRequestException(String message) {
        super(message);
        this.detail = null;
    }

    public BadRequestException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.detail = null;
    }

    public String getDetail() {
        return detail;
    }
}