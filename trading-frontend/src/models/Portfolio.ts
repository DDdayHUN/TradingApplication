import type {Trader} from "./Trader.ts";

export default interface Portfolio {
    id: string;
    capital: number;
    traders: Trader[];
}