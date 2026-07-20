CREATE TABLE app_users(
    id UUID PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO app_users (id, keycloak_sub)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'test-keycloak-sub'
);