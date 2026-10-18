import type {ReactElement} from "react";
import type Portfolio from "../../../models/Portfolio.ts";
import {NavLink} from "react-router";

interface PortfolioElementProps {
 item?: Portfolio
}

export default function PortfolioElement({item}: PortfolioElementProps): ReactElement {
    if(item == undefined) return (<></>)

    const activeTraders = item.traders.length;
    return (
        <NavLink to={`/portfolio/traders/`}>
            <div className ="w-150 h-40 flex m-1">
                <div className ="bg-gray-500 flex flex-col w-full h-full justify-center items-center">
                    <p>{item.id}</p>
                    <p>ibkrAvailable capital: {item.ibkrAvailableFunds}</p>
                    <p>IbkrLiquidation: {item.ibkrNetLiquidation}</p>
                    <p>DomainAvailableFunds: {item.availableCapital}</p>
                    <p>DomainLiquidation: {item.netLiquidation.toFixed(1)}</p>
                    <p>Active traders: {activeTraders}</p>
                </div>
            </div>
        </NavLink>
    )
}