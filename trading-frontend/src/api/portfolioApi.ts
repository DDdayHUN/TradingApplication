import {apiGet} from "./apiClient.ts";
import type Portfolio from "../models/Portfolio.ts";

export function getPortfolios(): Promise<Portfolio[]> {
    return apiGet<Portfolio[]>("/api/portfolio");
}

export function getPortfolio(portfolioId: string): Promise<Portfolio> {
    return apiGet<Portfolio>(`/api/portfolio/${portfolioId}`)
}