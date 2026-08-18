package exception.api;

import java.util.UUID;

public class PortfolioAlreadyExists extends RuntimeException {
    public PortfolioAlreadyExists(UUID id) {
        super("Portfolio already exists with id: " + id);
    }
}
