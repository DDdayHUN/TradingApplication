package exception;

import java.util.UUID;

public class PortfolioAlreadyExistsException extends RuntimeException {
    public PortfolioAlreadyExistsException(UUID id) {
        super("Portfolio already exists with id: " + id);
    }
}
