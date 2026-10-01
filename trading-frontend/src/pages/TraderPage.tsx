import {type ReactElement, useEffect, useState} from "react";

import TraderList from "../components/elements/lists/TraderList.tsx";
import type Portfolio from "../models/Portfolio.ts";
import {getPortfolio} from "../api/portfolioApi.ts";

export default function TraderPage(): ReactElement {

    const [portfolio, setPortfolio] = useState<Portfolio | null>(null);

    useEffect(() => {
        getPortfolio()
            .then(setPortfolio)
            .catch(e => console.error(e));


    }, []);

    return(
        <div className = "bg-gray-800 min-w-full min-h-full p-10">
            {!portfolio ? (
                <>

                </>
            ): (
                <TraderList portfolio={portfolio} />
            )}
        </div>
    )
}