import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import './i18n' // Initialize standard app translations

import IndexPage from './pages/index'
import RegisterPage from './pages/register'
import ConfirmEmailPage from './pages/confirmemail'
import FindAccountPage from './pages/findAccount'
import ResetPasswordPage from './pages/resetPassword'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<IndexPage />} />
        <Route path="/login" element={<Navigate to="/" replace />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/confirmemail" element={<ConfirmEmailPage />} />
        <Route path="/findAccount" element={<FindAccountPage />} />
        <Route path="/resetPassword" element={<ResetPasswordPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
