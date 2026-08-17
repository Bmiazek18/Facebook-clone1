import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { KcPage } from './kc.gen.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {window.kcContext ? (
      <KcPage kcContext={window.kcContext} />
    ) : (
      <App />
    )}
  </StrictMode>,
)
