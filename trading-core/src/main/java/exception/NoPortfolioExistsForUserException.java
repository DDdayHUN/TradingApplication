package exception;

import java.util.UUID;

public class NoPortfolioExistsForUserException extends RuntimeException {
    public NoPortfolioExistsForUserException(UUID userId) {
        super("Portfolio already exists with id: " + userId);
    }
}
