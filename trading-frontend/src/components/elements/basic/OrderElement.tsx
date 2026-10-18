import type {ReactElement} from "react";
import type Order from "../../../models/Order.ts";

interface OrderElementProps {
    item: Order
}

export default function OrderElement({item}: OrderElementProps): ReactElement{
    return (
        <div className = {`flex justify-between ${item.status == "PENDING" ? "bg-gray-600" : "bg-green-600"} m-1`}>
            <div>{item.securityIdentifier.tickerSymbol}</div>
            <div>{item.action}</div>
            <div>{item.status}</div>
            <div>{item.signalPrice}</div>
            <div>{item.filledPrice?.toFixed(1)}</div>
            <div>{item.timestamp}</div>
        </div>
    )
}