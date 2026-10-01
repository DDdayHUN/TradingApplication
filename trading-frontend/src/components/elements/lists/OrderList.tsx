import {type ReactElement} from "react";
import type Order from "../../../models/Order.ts";
import ListLayout from "../../../layouts/ListLayout.tsx";
import OrderElement from "../basic/OrderElement.tsx";

interface OrderListProps {
    orders: Order[];
}

export default function OrderList(props: OrderListProps): ReactElement{
    return (
        <ListLayout
            elements={props.orders}
            RowComponent={OrderElement}
            flexDirection={"flex-col"}
        />
    )
}