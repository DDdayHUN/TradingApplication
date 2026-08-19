import {type ReactElement, useEffect, useState} from "react";
import ListLayout from "../../../layouts/ListLayout.tsx";
import type Portfolio from "../../../models/Portfolio.ts";
import {getPortfolios} from "../../../api/portfolioApi.ts";
import PortfolioElement from "../basic/PortfolioElement.tsx";

export default function PortfolioList(): ReactElement {

    const [portfolios, setPortfolios] = useState<Portfolio[]> ([])


    useEffect(() => {
        getPortfolios()
            .then(setPortfolios)
            .catch(console.error)

    },[])

    return(
        <ListLayout
            elements={portfolios}
            RowComponent={PortfolioElement}
        />
    )
}