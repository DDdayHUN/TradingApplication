package exception.api;

import java.util.UUID;

public class HoldingNotFoundException extends RuntimeException {
   public HoldingNotFoundException(UUID holdingId) {
      super("Security holding not found with id: " + holdingId);
   }
}
