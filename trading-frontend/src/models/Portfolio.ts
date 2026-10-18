import type {Trader} from "./Trader.ts";

export default interface Portfolio {
    id: string;
    ibkrAvailableFunds: number;
    ibkrNetLiquidation: number;
    availableCapital: number;
    netLiquidation: number;
    traders: Trader[];
}