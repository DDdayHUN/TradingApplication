import type SecurityIdentifier from "./SecurityIdentifier.ts";

export default interface Order {
    id: string;
    securityIdentifier: SecurityIdentifier,
    action: string;
    status: string;
    signalPrice: number;
    filledPrice?: number;
}