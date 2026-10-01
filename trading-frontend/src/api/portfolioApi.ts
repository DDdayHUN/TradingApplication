import {apiGet} from "./apiClient.ts";
import type Portfolio from "../models/Portfolio.ts";
import type Order from "../models/Order.ts";

export function getPortfolio(): Promise<Portfolio> {
    return apiGet<Portfolio>(`/api/portfolio/`)
}

export function getOrders(): Promise<Order[]>{
    return apiGet<Order[]>(`/api/portfolio/orders/`);
}