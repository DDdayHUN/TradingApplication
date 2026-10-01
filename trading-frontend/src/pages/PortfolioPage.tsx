import {type ReactElement, useEffect, useState} from "react";
import PortfolioList from "../components/elements/lists/PortfolioList.tsx";
import {getOrders, getPortfolios} from "../api/portfolioApi.ts";
import type Portfolio from "../models/Portfolio.ts";
import type Order from "../models/Order.ts";
import OrderList from "../components/elements/lists/OrderList.tsx";

export default function PortfolioPage(): ReactElement {

    const [portfolios, setPortfolios] = useState<Portfolio[]> ([])
    const [orders, setOrders] = useState<Order[]> ([])

    useEffect(() => {
        getPortfolios()
            .then(async portfolios => {
                setPortfolios(portfolios)

                if (portfolios.length > 0) {
                    const orders = await getOrders(portfolios[0].id)
                    setOrders(orders)
                }
            }).catch(console.error)
    },[])

    return (
        <div className = "bg-gray-800 min-w-full min-h-full p-10 flex flex-col">
            <PortfolioList
                portfolios={portfolios}
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