import {apiGet, apiPost} from "./apiClient.ts";

interface ConnectIbkrRequest{
    port: number;
}

export function connectIbkr(request: ConnectIbkrRequest): Promise<void>{
    return apiPost("/api/ibkr/connect/", request)
}

export function isIbkrConnected(): Promise<boolean>{
    return apiGet("/api/ibkr/connected/");
}