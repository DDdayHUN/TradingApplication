import type {ReactElement} from "react";
import {connectIbkr} from "../api/IbkrApi.ts";

export default function IbkrPage(): ReactElement {

    const handleConnect = async () => {
        await connectIbkr({
            port: 4002
        });
    };

    return (
        <div>
            <button
            type = "submit"
            onClick = {handleConnect}
            >
            connect
            </button>
        </div>
    )
}