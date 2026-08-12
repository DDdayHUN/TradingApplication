package exception.api;

import java.util.UUID;

public class PortfolioNotFoundException extends RuntimeException {
   public PortfolioNotFoundException(String keycloakSub) {
      super("Could not find portfolio for user with keycloakSub: " + keycloakSub);
   }

   public PortfolioNotFoundException(UUID id) {
      super("Could not find portfolio for user with id: " + id);
   }
}
