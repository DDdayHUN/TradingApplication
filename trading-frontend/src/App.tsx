import './App.css'
import {Route, Routes} from "react-router";
import HomePage from "./components/pages/HomePage.tsx";

function App() {
  return (
    <>
        <Routes>
         <Route path="/" element={<HomePage />} />
        </Routes>
    </>
  )
}

export default App
