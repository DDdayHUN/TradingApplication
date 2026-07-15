import './App.css'
import logo from './assets/logo.png'

function App() {
  return (
    <>
        <div className = "flex w-full justify-center bg-gray-500 h-10 items-center">
            <p className = "text-l">Trading App</p>
        </div>
        <div className = "flex justify-center items-center h-full w-full">
            <img src = {logo} alt="logo" className = "w-200" />
        </div>
    </>
  )
}

export default App
