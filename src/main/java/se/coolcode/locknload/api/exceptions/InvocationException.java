package se.coolcode.locknload.api.exceptions;

public class InvocationException extends RuntimeException {

    public InvocationException(String message, Exception e) {
        super(message, e);
    }
}
