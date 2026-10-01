import {type ReactElement} from "react";
import ListLayout from "../../../layouts/ListLayout.tsx";
import type Portfolio from "../../../models/Portfolio.ts";
import PortfolioElement from "../basic/PortfolioElement.tsx";

interface PortfolioListProps {
    portfolios: Portfolio[];
}

export default function PortfolioList(props: PortfolioListProps): ReactElement {

    return(
        <ListLayout
            elements={props.portfolios}
            RowComponent={PortfolioElement}
            flexDirection={"flex-row"}
        />
    )
}