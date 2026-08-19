import type {ReactElement} from "react";
import type Portfolio from "../../../models/Portfolio.ts";
import {NavLink} from "react-router";

interface PortfolioElementProps {
 item: Portfolio
}

export default function PortfolioElement({item}: PortfolioElementProps): ReactElement {
    return (
        <NavLink to={`/portfolio/${item.id}/traders`}>
            <div className ="pb-40">
                {item.id}
            </div>
        </NavLink>
    )
}