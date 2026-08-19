import {type ReactElement, useEffect, useState} from "react";

import TraderList from "../components/elements/lists/TraderList.tsx";
import {useParams} from "react-router";
import type Portfolio from "../models/Portfolio.ts";
import {getPortfolio} from "../api/portfolioApi.ts";

export default function TraderPage(): ReactElement {
    const { portfolioId } = useParams();

    const [portfolio, setPortfolio] = useState<Portfolio | null>(null);

    useEffect(() => {
        if(!portfolioId) return;

            getPortfolio(portfolioId)
                .then(setPortfolio)
                .catch(e => console.error(e));


    }, [portfolioId]);

    if(!portfolio){
        return (
            <div>Loading...</div>
        );
    }

    return(
        <div className = "bg-gray-800 min-w-full min-h-full pt-10 pl-10">
            <TraderList portfolio={portfolio} />
        </div>
    )
}