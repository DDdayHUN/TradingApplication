import {apiGet} from "./apiClient.ts";

export interface UserResponse {
    id: string;
    userName: string;
}

export function getCurrentUser(): Promise<UserResponse> {
    return apiGet<UserResponse>("/api/users");
}