import type {ReactElement} from "react";
import PortfolioList from "../components/elements/lists/PortfolioList.tsx";

export default function PortfolioPage(): ReactElement {
    return (
        <div className = "bg-gray-800 min-w-full min-h-full pt-10 pl-10">
            <PortfolioList />
        </div>
    )
}