package exception.api;

import java.util.UUID;

public class TraderHoldingsNotEmptyException extends RuntimeException {
   public TraderHoldingsNotEmptyException(UUID traderId) {
      super("Traders holdings with id: " + traderId + ", is not empty");
   }
}
