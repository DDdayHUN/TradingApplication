import {type ReactElement, useState} from "react";
import {connect, disconnect} from "../api/IbkrApi.ts";

export default function IbkrPage(): ReactElement {

    const [isConnected, setConnected] = useState<boolean>(false);
    const handleConnect = async () => {
        setConnected(await connect({port: 4002}));
    };

    const handleDisconnect = async () => {
        setConnected(await disconnect());
    }



    return (
        <div className = "bg-gray-800 min-w-full min-h-full p-10 flex flex-col gap-5">
            <button type = "submit" onClick = {handleConnect}
            className = "hover:cursor-pointer w-20 h-10 bg-green-400">
            connect
            </button>

            <button type = "submit" onClick = {handleDisconnect}
                    className = "hover:cursor-pointer w-20 h-10 bg-red-400">
                disconnect
            </button>
            {isConnected && (
                <div>
                    The bluetooth device has been connected successfully.
                </div>
            )}
        </div>
    )
}