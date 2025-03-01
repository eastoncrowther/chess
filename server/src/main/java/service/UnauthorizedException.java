package service;

/**
 * Indicates that the provided auth token was not valid
 */
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}
