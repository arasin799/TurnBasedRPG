package rpg.exception;

public class InvalidSaveException extends Exception {

    public InvalidSaveException(String message) {
        super(message);
    }

    public InvalidSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}