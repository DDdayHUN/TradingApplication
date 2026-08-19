import './App.css'
import {Route, Routes} from "react-router";
import HomePage from "./pages/HomePage.tsx";
import Sidebar from "./components/sidebar/Sidebar.tsx";
import PortfolioPage from "./pages/PortfolioPage.tsx";
import BacktestPage from "./pages/BacktestPage.tsx";

function App() {
  return (
    <div className = "w-full h-full flex">
        <Sidebar />

        <main className="flex-1 h-full">
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/portfolio" element={<PortfolioPage />} />
                <Route path="/backtest" element={<BacktestPage />} />
            </Routes>
        </main>
    </div>
  )
}

export default App
