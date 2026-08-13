package exception.api;

import java.util.UUID;

public class UserAlreadyExistsException extends RuntimeException {
   public UserAlreadyExistsException(UUID id) {
      super("User with ID " + id + " already exists");
   }
}
