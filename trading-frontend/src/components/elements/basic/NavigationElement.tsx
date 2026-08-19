import type {ReactElement} from "react";
import {NavLink} from "react-router";

interface NavigationElementProps{
    text: string,
    icon?: string,
    iconClassName?: string,
    to: string
}

export default function NavigationElement(props: NavigationElementProps): ReactElement {

    return (
        <NavLink
            to = {props.to}
            className="relative w-35 h-10 flex items-center hover:bg-gray-700
             rounded-md pl-3 hover:border-l-2 border-[#12BC7D]
             "
        >
            {props.icon && (
                <img
                    src={props.icon}
                    alt="Navigation element icon"
                    className= {props.iconClassName}
                />
            )}

            <div className="w-full text-left pl-2 text-white text-[14px]">
                {props.text}
            </div>
        </NavLink>
    )
}