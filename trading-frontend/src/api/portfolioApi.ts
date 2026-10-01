import {apiGet} from "./apiClient.ts";
import type Portfolio from "../models/Portfolio.ts";
import type Order from "../models/Order.ts";

export function getPortfolios(): Promise<Portfolio[]> {
    return apiGet<Portfolio[]>("/api/portfolio");
}

export function getPortfolio(portfolioId: string): Promise<Portfolio> {
    return apiGet<Portfolio>(`/api/portfolio/${portfolioId}`)
}

export function getOrders(portfolioId: string): Promise<Order[]>{
    return apiGet<Order[]>(`/api/portfolio/${portfolioId}/orders`);
}