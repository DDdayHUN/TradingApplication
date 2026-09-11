import type {ReactElement} from "react";
import type {Trader} from "../../../models/Trader.ts";

interface TraderElementProps{
    item: Trader
}

export default function TraderElement({item}: TraderElementProps): ReactElement {
    return(
        <div className = "flex flex-col w-min-full h-full bg-red-800 m-1">
            <div>{item.id}</div>
            <div>{item.capital}</div>
            <div>
                <p>Security identifier: {item.securityIdentifier.isin}, {item.securityIdentifier.tickerSymbol}, {item.securityIdentifier.currency}</p>
            </div>
            <div>
                {item.holdings.map((holding) => (
                    <div key={holding.id}>
                        <p>Amount: {holding.amount}</p>
                        <p>Entry price: {holding.entryPrice}</p>
                    </div>
                ))}
            </div>
        </div>
    )
}