import {type ReactElement, useEffect, useState} from "react";
import {getOrders, getPortfolio} from "../api/portfolioApi.ts";
import type Portfolio from "../models/Portfolio.ts";
import type Order from "../models/Order.ts";
import OrderList from "../components/elements/lists/OrderList.tsx";
import PortfolioElement from "../components/elements/basic/PortfolioElement.tsx";

export default function PortfolioPage(): ReactElement {

    const [portfolio, setPortfolio] = useState<Portfolio> ()
    const [orders, setOrders] = useState<Order[]> ([])

    useEffect(() => {
        getPortfolio()
            .then(async portfolio => {
                setPortfolio(portfolio)

                if (portfolio != null || portfolio != undefined) {
                    const orders = await getOrders()
                    setOrders(orders)
                }
            }).catch(console.error)
    },[])

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