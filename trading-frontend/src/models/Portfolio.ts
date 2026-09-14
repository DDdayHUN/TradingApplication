import type {Trader} from "./Trader.ts";

export default interface Portfolio {
    id: string;
    availableCapital: number;
    accountLiquidation: number;
    traders: Trader[];
}