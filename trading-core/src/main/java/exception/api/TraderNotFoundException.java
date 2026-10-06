package exception.api;

import java.util.UUID;

public class TraderNotFoundException extends RuntimeException {
   public TraderNotFoundException(UUID id) {
      super("Trader with id: " + id + "not found. ");
   }

   public TraderNotFoundException(Iterable<UUID> ids) {
      super("One or more of the given ids were not found: " + ids);
   }
}
