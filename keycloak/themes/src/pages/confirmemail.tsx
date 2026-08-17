import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, Info } from 'lucide-react'
import AuthButton from '~/components/AuthButton'

export const ConfirmEmailPage: React.FC = () => {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [verificationCode, setVerificationCode] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isResending, setIsResending] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  useEffect(() => {
    const fetchVerifyInfo = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/auth/verify-info', {
          method: 'GET',
          credentials: 'include'
        })

        if (!response.ok) {
          setErrorMessage('Sesja wygasła. Wróć do logowania.')
          return
        }

        const data = await response.text()
        setEmail(data)
      } catch (error) {
        console.error('Błąd pobierania danych:', error)
      }
    }
    fetchVerifyInfo()
  }, [])

  const handleContinue = async (e: React.FormEvent) => {
    e.preventDefault()
    if (verificationCode.length !== 6) {
      setErrorMessage('Wprowadź poprawny 6-cyfrowy kod.')
      return
    }

    setIsSubmitting(true)
    setErrorMessage('')
    setSuccessMessage('')

    try {
      const url = `http://localhost:8080/api/auth/verify?code=${encodeURIComponent(verificationCode)}`
      const response = await fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      })

      const data = await response.text()

      if (!response.ok) {
        throw new Error(data || 'Coś poszło nie tak podczas weryfikacji.')
      }

      setSuccessMessage(data)
    } catch (error: any) {
      setErrorMessage(error.message || String(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleResendCode = async () => {
    setIsResending(true)
    setErrorMessage('')
    setSuccessMessage('')

    try {
      const response = await fetch('http://localhost:8080/api/auth/resend-code', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      })

      const data = await response.text()

      if (!response.ok) {
        throw new Error(data || 'Nie udało się wysłać kodu ponownie.')
      }

      setSuccessMessage(data)
    } catch (error: any) {
      setErrorMessage(error.message || String(error))
    } finally {
      setIsResending(false)
    }
  }

  return (
    <div className="flex flex-col min-h-screen bg-white font-sans text-[#1c1e21] pt-10 px-4">
      <div className="w-full max-w-[440px] self-center flex flex-col flex-grow">
        <header className="mb-5 text-left">
          <button
            onClick={() => navigate(-1)}
            className="text-gray-600 hover:text-black transition cursor-pointer"
          >
            <ChevronLeft size={24} />
          </button>
        </header>

        <main className="w-full text-left">
          {errorMessage && (
            <div className="mb-5 p-4 text-[14px] text-black bg-white rounded-2xl border border-gray-200 shadow-sm flex items-start gap-3">
              <Info className="text-red-500 shrink-0 mt-0.5" size={20} />
              <span>{errorMessage}</span>
            </div>
          )}

          {successMessage && (
            <div className="mb-5 p-4 text-[14px] text-black bg-white rounded-2xl border border-gray-200 shadow-sm flex items-start gap-3">
              <Info className="text-green-500 shrink-0 mt-0.5" size={20} />
              <span>{successMessage}</span>
            </div>
          )}

          <h1 className="text-[20px] font-bold tracking-tight mb-2 text-[#0f1419]">
            Potwierdź swoje konto
          </h1>

          <p className="text-[14px] leading-normal text-[#536471] mb-5">
            Wysłaliśmy kod na Twój adres e-mail
            {email && (
              <>
                : <span className="font-semibold text-black">{email}</span>
              </>
            )}
            . Wprowadź ten kod, aby potwierdzić swoje konto.
          </p>

          <form className="space-y-4" onSubmit={handleContinue}>
            <div>
              <input
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value)}
                type="text"
                maxLength={6}
                placeholder="Wprowadź kod"
                className="w-full px-4 py-3.5 border border-[#ccd0d5] rounded-xl text-[15px] placeholder-[#606770] focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition duration-150 text-left"
              />
            </div>

            <AuthButton
              type="submit"
              disabled={verificationCode.length !== 6 || isSubmitting}
            >
              {isSubmitting ? 'Sprawdzanie...' : 'Kontynuuj'}
            </AuthButton>

            <button
              type="button"
              disabled={isResending}
              className="w-full bg-white hover:bg-gray-50 text-black border border-gray-300 font-semibold py-3 px-4 rounded-full text-[15px] transition duration-150 cursor-pointer disabled:opacity-50"
              onClick={handleResendCode}
            >
              {isResending ? 'Wysyłanie...' : 'Nie masz kodu?'}
            </button>
          </form>
        </main>

        <footer className="w-full text-[12px] text-[#737373] mt-auto pt-12 pb-6">
          <div className="flex flex-wrap gap-x-3 gap-y-1 mb-3 text-[#737373] text-left">
            <span className="cursor-pointer hover:underline text-gray-500">Polski</span>
            <a href="#" className="hover:underline text-[#385898]">English (US)</a>
            <a href="#" className="hover:underline text-[#385898]">ślōnshō gōdka</a>
            <a href="#" className="hover:underline text-[#385898]">Русский</a>
            <a href="#" className="hover:underline text-[#385898]">Deutsch</a>
            <a href="#" className="hover:underline text-[#385898]">Français (France)</a>
            <a href="#" className="hover:underline text-[#385898]">Italiano</a>
            <a href="#" className="hover:underline text-[#385898]">Więcej języków...</a>
          </div>

          <div className="border-b border-[#e5e5e5] my-2" />

          <div className="flex flex-wrap gap-x-3 gap-y-1 mb-4 text-left">
            <a href="#" className="hover:underline">Zarejestruj się</a>
            <a href="#" className="hover:underline">Zaloguj się</a>
            <a href="#" className="hover:underline">Messenger</a>
            <a href="#" className="hover:underline">Facebook Lite</a>
            <a href="#" className="hover:underline">Film</a>
            <a href="#" className="hover:underline">Meta Pay</a>
            <a href="#" className="hover:underline">Sklep Meta</a>
            <a href="#" className="hover:underline">Meta Quest</a>
            <a href="#" className="hover:underline">Ray-Ban Meta</a>
            <a href="#" className="hover:underline">Meta AI</a>
            <a href="#" className="hover:underline">Instagram</a>
            <a href="#" className="hover:underline">Threads</a>
            <a href="#" className="hover:underline">Zasady ochrony prywatności</a>
            <a href="#" className="hover:underline">Centrum ochrony prywatności</a>
          </div>

          <div className="text-[#737373] text-left">
            Meta © 2026
          </div>
        </footer>
      </div>
    </div>
  )
}

export default ConfirmEmailPage
