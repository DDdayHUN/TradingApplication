import type {ReactElement} from "react";

export default function HomePage(): ReactElement {
    return (
        <>
            <div className = "flex justify-around items-center w-full h-20 bg-gray-800">
                <div className="text-xl text-white font-semibold">Trading</div>
            </div>
        </>
    )
}