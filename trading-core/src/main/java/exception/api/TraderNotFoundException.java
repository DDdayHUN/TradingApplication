package exception.api;

import java.util.UUID;

public class TraderNotFoundException extends RuntimeException {
   public TraderNotFoundException(UUID id, UUID userId) {
      super("Trader with id " + id + "not found for user with id " + userId );
   }
}
