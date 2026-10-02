import './App.css'
import {Route, Routes} from "react-router";
import Sidebar from "./components/sidebar/Sidebar.tsx";
import PortfolioPage from "./pages/PortfolioPage.tsx";
import IbkrPage from "./pages/IbkrPage.tsx";
import TraderPage from "./pages/TraderPage.tsx";

function App() {
  return (
    <div className = "w-full h-screen flex overflow-hidden">
        <Sidebar />

        <main className="flex-1 h-screen overflow-y-auto">
            <Routes>
                <Route path="/" element={<PortfolioPage />} />
                <Route path="/portfolio/" element={<PortfolioPage />} />
                <Route path="/portfolio/traders/" element={<TraderPage/>} />
                <Route path="/ibkr/" element={<IbkrPage />} />
            </Routes>
        </main>
    </div>
  )
}

export default App
