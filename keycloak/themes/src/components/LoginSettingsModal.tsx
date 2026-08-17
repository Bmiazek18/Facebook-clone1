import React from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'

interface UserProfile {
  id: number
  name: string
  avatar: string
}

interface LoginSettingsModalProps {
  isOpen: boolean
  onClose: () => void
  profiles: UserProfile[]
  onRemoveProfile?: (id: number) => void
}

export const LoginSettingsModal: React.FC<LoginSettingsModalProps> = ({
  isOpen,
  onClose,
  profiles,
  onRemoveProfile
}) => {
  if (!isOpen) return null

  const handleRemove = (id: number) => {
    onRemoveProfile?.(id)
  }

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60">
      <div
        className="absolute inset-0"
        onClick={onClose}
      />
      <div className="relative bg-white w-full max-w-[550px] rounded-[24px] p-8 shadow-2xl z-10 animate-fade-in-up">
        <button
          className="absolute top-6 right-6 text-slate-800 hover:bg-slate-100 p-2 rounded-full transition-colors cursor-pointer"
          onClick={onClose}
        >
          <X size={28} />
        </button>

        <h2 className="text-[26px] font-semibold text-slate-900 mb-8 mt-2 tracking-tight">
          Usuń profile z tej przeglądarki
        </h2>

        {profiles.map((profile) => (
          <div
            key={profile.id}
            className="flex items-center justify-between border border-slate-200 rounded-xl p-4 mb-6"
          >
            <div className="flex items-center gap-4">
              <div className="w-[50px] h-[50px] rounded-full overflow-hidden border border-slate-100">
                <img
                  src={profile.avatar}
                  alt={profile.name}
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="flex flex-col text-left">
                <span className="text-[17px] font-medium text-slate-900">{profile.name}</span>
                <span className="text-[15px] text-slate-500">Facebook</span>
              </div>
            </div>
            <button
              onClick={() => handleRemove(profile.id)}
              className="px-6 py-2 border border-slate-300 rounded-full font-medium text-slate-900 bg-white hover:bg-slate-50 transition-colors text-[15px] cursor-pointer"
            >
              Usuń
            </button>
          </div>
        ))}

        <p className="text-[15px] text-slate-500 text-left">
          <a
            href="#"
            className="text-primary font-semibold hover:underline"
          >Dowiedz się więcej</a>
          {" "}na temat tego, dlaczego widzisz tutaj profile i co oznacza ich usunięcie.
        </p>
      </div>
    </div>,
    document.body
  )
}

export default LoginSettingsModal
