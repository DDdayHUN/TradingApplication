package api.exception.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
   public UserNotFoundException(String keycloakSub){
      super("User not found:" + keycloakSub);
   }

   public UserNotFoundException(UUID id){
      super("User not found:" + id);
   }
}
