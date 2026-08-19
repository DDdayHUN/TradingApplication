import keycloak from "../auth/keycloak.ts";

const API_URL = import.meta.env.VITE_API_URL;

async function apiFetch(
    path: string,
    options: RequestInit = {},
): Promise<Response>{
    await keycloak.updateToken(30);

    if(!keycloak.token) throw new Error("No Keycloak access token");

    return fetch(`${API_URL}${path}`, {
        ...options,
        headers: {
            ...options.headers,
            Authorization: `Bearer ${keycloak.token}`,
            "Content-Type": "application/json",
        },
    });
}

export async function apiGet<T>(path: string): Promise<T> {
    const response = await apiFetch(path);
    return await response.json() as Promise<T>;
}

export async function apiPost<T>(
    path: string,
    body: unknown,
): Promise<T> {
    const response = await apiFetch(path, {
        method: "POST",
        body: JSON.stringify(body),
    });

    return await response.json() as Promise<T>;
}