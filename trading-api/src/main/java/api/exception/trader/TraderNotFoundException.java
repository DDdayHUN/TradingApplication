package api.exception.trader;

import java.util.UUID;

public class TraderNotFoundException extends RuntimeException {
   public TraderNotFoundException(UUID id, String keycloakSub) {
      super("Trader with id " + id + "not found for user with keycloakSub " + keycloakSub );
   }
}
