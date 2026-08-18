package exception.api;

import java.util.UUID;

public class PortfolioNotFoundException extends RuntimeException {
   public PortfolioNotFoundException(UUID id) {
      super("Portfolio not found with id: " + id);
   }
}
