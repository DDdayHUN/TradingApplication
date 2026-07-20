package api.exception

class UserAlreadyExistsException(keycloakSub: String) : RuntimeException(
    "User with keycloak subject '$keycloakSub' already exists"
)