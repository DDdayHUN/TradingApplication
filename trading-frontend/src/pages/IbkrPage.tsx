import type {ReactElement} from "react";
import {connectIbkr} from "../api/IbkrApi.ts";

export default function IbkrPage(): ReactElement {

    const handleConnect = async () => {
        await connectIbkr({
            port: 4002
        });
    };

    return (
        <div className = "bg-gray-800 min-w-full min-h-full p-10">
            <button type = "submit" onClick = {handleConnect}
            className = "hover:cursor-pointer w-20 h-10 bg-green-400">
            connect
            </button>
        </div>
    )
}