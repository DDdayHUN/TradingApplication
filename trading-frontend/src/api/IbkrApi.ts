import {apiGet, apiPost} from "./apiClient.ts";

interface ConnectIbkrRequest{
    port: number;
}

export function connect(request: ConnectIbkrRequest): Promise<boolean>{
    return apiPost("/api/ibkr/connect/", request)
}

export function disconnect(): Promise<boolean>{
    return apiPost("/api/ibkr/disconnect/", null)
}

export function isIbkrConnected(): Promise<boolean>{
    return apiGet("/api/ibkr/connected/");
}