package exception;

//===========================================================//
//===========================================================//
/**
 * Thrown when the application enters an unrecoverable state and must exit gracefully.
 *
 * <p>To emphasize this the class extends {@link Error} and it must not be caught inside try catch
 * blocks, allowing it to propagate to the top-level shutdown handler.
 */
public class UnrecoverableStateException extends Error {
    public UnrecoverableStateException(String message) { super(message); }
    public UnrecoverableStateException(String message, Throwable cause) { super(message, cause); }
    public UnrecoverableStateException(Throwable cause) { super(cause); }
}