import keycloak from "../auth/keycloak.ts";

export async function apiFetch(
    path: string,
    options: RequestInit = {},
): Promise<Response>{
    await keycloak.updateToken(30);

    if(!keycloak.token) throw new Error("No Keycloak access token");

    return fetch(path, {
        ...options,
        headers: {
            ...options.headers,
            Authorization: `Bearer ${keycloak.token}`,
            "Content-Type": "application/json",
        },
    });
}