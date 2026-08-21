import type {ReactElement} from "react";
import type {Trader} from "../../../models/Trader.ts";

interface TraderElementProps{
    item: Trader
}

export default function TraderElement({item}: TraderElementProps): ReactElement {
    return(
        <div>
            {item.id}
        </div>
    )
}