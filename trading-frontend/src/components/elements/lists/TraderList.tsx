import type {ReactElement} from "react";
import ListLayout from "../../../layouts/ListLayout.tsx";
import type Portfolio from "../../../models/Portfolio.ts";
import TraderElement from "../basic/TraderElement.tsx";

interface TraderListProps {
    portfolio: Portfolio
}

export default function TraderList({portfolio}: TraderListProps): ReactElement {

    return(
        <ListLayout
          elements={portfolio.traders}
          RowComponent={TraderElement}
          flexDirection={"flex-col"}
        />
    )
}