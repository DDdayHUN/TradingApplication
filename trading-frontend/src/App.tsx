import './App.css'
import {Route, Routes} from "react-router";
import HomePage from "./pages/HomePage.tsx";
import Sidebar from "./components/sidebar/Sidebar.tsx";
import PortfolioPage from "./pages/PortfolioPage.tsx";
import BacktestPage from "./pages/BacktestPage.tsx";
import TraderPage from "./pages/TraderPage.tsx";

function App() {
  return (
    <div className = "w-full h-screen flex overflow-hidden">
        <Sidebar />

        <main className="flex-1 h-screen overflow-y-auto">
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/portfolio" element={<PortfolioPage />} />
                <Route path="/portfolio/:portfolioId/traders" element={<TraderPage/>} />
                <Route path="/backtest" element={<BacktestPage />} />
            </Routes>
        </main>
    </div>
  )
}

export default App
