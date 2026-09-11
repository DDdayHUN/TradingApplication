import type {ReactElement} from "react";
import type Portfolio from "../../../models/Portfolio.ts";
import {NavLink} from "react-router";

interface PortfolioElementProps {
 item: Portfolio
}

export default function PortfolioElement({item}: PortfolioElementProps): ReactElement {

    const activeTraders = item.traders.length;
    return (
        <NavLink to={`/portfolio/${item.id}/traders`}>
            <div className ="w-150 h-40 flex m-1">
                <div className ="bg-gray-500 flex flex-col w-full h-full justify-center items-center">
                    <p>{item.id}</p>
                    <p>Available capital: {item.availableCapital}</p>
                    <p>Liquidation: {item.accountLiquidation}</p>
                    <p>Active traders: {activeTraders}</p>
                </div>
            </div>
        </NavLink>
    )
}