import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import keycloak from "./auth/keycloak.ts";


async function startApplication(): Promise<void>{
    try{
        await keycloak.init({
            onLoad: "login-required",
            pkceMethod: "S256",
            checkLoginIframe: false
        });

        createRoot(document.getElementById('root')!).render(
            <StrictMode>
                <App />
            </StrictMode>,
        )
    } catch(error){
        console.error("keycloak init failes",error)
    }
}

void startApplication()


