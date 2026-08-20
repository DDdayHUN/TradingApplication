import type {ReactElement} from "react";
import type Portfolio from "../../../models/Portfolio.ts";
import {NavLink} from "react-router";

interface PortfolioElementProps {
 item: Portfolio
}

export default function PortfolioElement({item}: PortfolioElementProps): ReactElement {
    return (
        <NavLink to={`/portfolio/${item.id}/traders`}>
            <div className ="w-150 h-40 flex m-1">
                <div className ="bg-gray-500 flex items-center w-full h-full justify-center">
                    {item.id}
                </div>
            </div>
        </NavLink>
    )
}