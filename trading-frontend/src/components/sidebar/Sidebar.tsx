import {type ReactElement, useEffect, useState} from "react";
import NavigationElement from "../elements/basic/NavigationElement.tsx";
import logo from "../../../public/logo.png"
import homeIcon from "../../assets/icons/home.svg"
import portfolioIcon from "../../assets/icons/portfolio.svg"
import terminalIcon from "../../assets/icons/terminal.svg"
import {NavLink} from "react-router";
import {getCurrentUser, type UserResponse} from "../../api/userApi.ts";

export default function Sidebar(): ReactElement {
    const [user, setUser] = useState<UserResponse | null>(null);

    useEffect(() => {
        getCurrentUser()
            .then(setUser)
            .catch(console.error);
    }, [])
    return (
        <>
            <div className = "w-20 md:w-25 lg:w-40 min-h-full bg-gray-800 flex items-start border-r-2 border-gray-200 flex-col pl-1">
                <NavLink className = "flex flex-row pt-2" to="/">
                    <img
                        src = {logo}
                        alt = "Trading logo"
                        className = "h-10 w-10 object-contain"
                    />
                    <div className = "flex h-10 items-center justify-center font-semibold text-white ">
                        Trading
                    </div>
                </NavLink>

                <div className = "flex-1 h-80 justify-center pt-3">
                    <NavigationElement text = "Home" icon = {homeIcon} iconClassName = "h-5 object-contain" to= "/"/>
                    <NavigationElement text = "Portfolio" icon = {portfolioIcon} iconClassName= "h-5 object-contain" to="/portfolio"/>
                    <NavigationElement text = "Backtest" icon = {terminalIcon} iconClassName= "h-5 object-contain" to="/backtest" />
                </div>

                <div>
                    user: {user?.id}
                </div>
            </div>
        </>
    )
}