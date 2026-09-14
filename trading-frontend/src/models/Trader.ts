import type SecurityIdentifier from "./SecurityIdentifier.ts";
import type SecurityHolding from "./SecurityHolding.ts";

export interface Trader {
    id: string;
    securityIdentifier: SecurityIdentifier;
    capital: number;
    holdings: SecurityHolding[];
    algorithmType: string;
}