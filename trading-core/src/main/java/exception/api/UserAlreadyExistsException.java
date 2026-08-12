package exception.api;

public class UserAlreadyExistsException extends RuntimeException {
   public UserAlreadyExistsException(String keycloakSub) {
      super("User with Keycloak subject " + keycloakSub + " already exists");
   }
}
