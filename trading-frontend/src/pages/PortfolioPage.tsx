import {type ReactElement, useEffect, useState} from "react";
import {getOrders, getPortfolio} from "../api/portfolioApi.ts";
import type Portfolio from "../models/Portfolio.ts";
import type Order from "../models/Order.ts";
import OrderList from "../components/elements/lists/OrderList.tsx";
import PortfolioElement from "../components/elements/basic/PortfolioElement.tsx";
import {isIbkrConnected} from "../api/IbkrApi.ts";

export default function PortfolioPage(): ReactElement {

    const [portfolio, setPortfolio] = useState<Portfolio> ()
    const [orders, setOrders] = useState<Order[]> ([])
    const [connected, setConnected] = useState<boolean>(false)

    useEffect(() => {
        const load = async () => {
            try {
                const isConnected = await isIbkrConnected();

                setConnected(isConnected);

                if (!isConnected) return;

                const portfolio = await getPortfolio();
                setPortfolio(portfolio);

                if (portfolio) {
                    const orders = await getOrders();
                    setOrders(orders);
                }
            } catch (error) {
                console.error(error);
            }
        };

        load();
    }, []);

    if (!connected) {
        return (
            <div className="bg-gray-800 min-w-full min-h-full p-10">
                <p className="text-white">
                    Connect to IBKR first.
                </p>
            </div>
        );
    }

    return (
        <div className = "bg-gray-800 min-w-full min-h-full p-10 flex flex-col">
            <PortfolioElement
             item = {portfolio}
            />
            <p className ="text-white m-1">Orders:</p>
            <div className="bg-gray-600 flex-1">
                <OrderList
                    orders = {orders}
                />
            </div>
        </div>
    )
}