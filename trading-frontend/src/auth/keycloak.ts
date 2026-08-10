import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://localhost:8081",
    realm: "trading",
    clientId: "trading-frontend"
});

export default keycloak;

