package exception;

import java.util.UUID;

public class TraderHoldingsNotEmptyException extends RuntimeException {
   public TraderHoldingsNotEmptyException(UUID traderId) {
      super("Traders holdings with id: " + traderId + ", is not empty");
   }

   public TraderHoldingsNotEmptyException(Iterable<UUID> ids) {
      super("One or more of the given ids have non empty holdings: " + ids);
   }
}
