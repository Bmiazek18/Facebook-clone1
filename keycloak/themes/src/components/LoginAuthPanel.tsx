import React, { useState, useRef, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Settings, ChevronRight, ChevronLeft, Infinity as MetaIcon } from 'lucide-react'
import CustomInput from './CustomInput'
import type { CustomInputRef } from './CustomInput'
import AuthButton from './AuthButton'
import LoginSettingsModal from './LoginSettingsModal'

interface UserProfile {
  id: number
  name: string
  username: string
  avatar: string
  hasActiveSession?: boolean
  lastLogin?: number
}

const getNameFromEmail = (emailStr: string) => {
  const part = emailStr.includes("@") ? emailStr.split("@")[0] : emailStr;
  return part
    .split(/[._-]/)
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

export const LoginAuthPanel: React.FC = () => {
  const [profiles, setProfiles] = useState<UserProfile[]>([]);

  const [showLoginForm, setShowLoginForm] = useState(() => {
    if (typeof window !== "undefined") {
      const saved = localStorage.getItem("recent_profiles");
      const profilesList = saved ? JSON.parse(saved) : [];
      return profilesList.length === 0;
    }
    return false;
  });

  const [showSettingsModal, setShowSettingsModal] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const emailInputRef = useRef<CustomInputRef | null>(null)

  // Stany dla formularza
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  // Sync profiles and session cookie status
  useEffect(() => {
    if (typeof window === "undefined") return;

    const hasSessionCookie = document.cookie.split(';').some(item => item.trim().startsWith('jwt_token='));
    
    const saved = localStorage.getItem("recent_profiles");
    let profilesList: UserProfile[] = saved ? JSON.parse(saved) : [];

    // Check if session cookies are present. If not, set hasActiveSession to false for all.
    if (!hasSessionCookie) {
      profilesList = profilesList.map((p) => ({ ...p, hasActiveSession: false }));
      localStorage.setItem("recent_profiles", JSON.stringify(profilesList));
    }

    setProfiles(profilesList);
  }, []);

  // Focus email input when login form is opened
  useEffect(() => {
    if (showLoginForm) {
      setTimeout(() => {
        emailInputRef.current?.focus()
      }, 50)
    }
  }, [showLoginForm])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!email || !password) {
      setErrorMessage('Wprowadź e-mail/telefon oraz hasło.')
      return
    }

    setIsLoading(true)
    setErrorMessage('')

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          emailOrPhone: email,
          password: password
        })
      })

      const data = await response.text()

      if (!response.ok) {
        throw new Error(data || 'Nieprawidłowe dane logowania.')
      }

      // Save cookie
      document.cookie = `jwt_token=${encodeURIComponent(data)}; path=/; max-age=${60 * 60 * 24 * 7}; SameSite=Lax`

      // Add/update profiles list in localStorage
      const saved = localStorage.getItem("recent_profiles");
      let profilesList = saved ? JSON.parse(saved) : [];
      profilesList = profilesList.filter((p: any) => p.username.toLowerCase() !== email.toLowerCase());
      const newProfile = {
        id: Date.now(),
        name: getNameFromEmail(email),
        username: email,
        avatar: `https://images.unsplash.com/photo-1640951613773-54706e06851d?w=100&auto=format&fit=crop&q=60`,
        hasActiveSession: true,
        lastLogin: Date.now()
      };
      profilesList.unshift(newProfile);
      localStorage.setItem("recent_profiles", JSON.stringify(profilesList));

      // Redirect
      window.location.href = '/'
    } catch (error: any) {
      setErrorMessage(error.message || String(error))
    } finally {
      setIsLoading(false)
    }
  }

  const handleRemoveProfile = (id: number) => {
    const updated = profiles.filter((p) => p.id !== id);
    setProfiles(updated);
    localStorage.setItem("recent_profiles", JSON.stringify(updated));
    
    if (updated.length === 0) {
      setShowLoginForm(true);
    }
  };

  const handleProfileClick = (profile: UserProfile) => {
    if (profile.hasActiveSession) {
      // Simulate one-click login in mock: cookie is already active (or set it and redirect)
      const hasSessionCookie = document.cookie.split(';').some(item => item.trim().startsWith('jwt_token='));
      if (hasSessionCookie) {
        window.location.href = '/';
      } else {
        // Cookie somehow missing, fallback to password
        setEmail(profile.username);
        setShowLoginForm(true);
      }
    } else {
      // Fallback: fill email and request password
      setEmail(profile.username);
      setShowLoginForm(true);
    }
  }

  return (
    <section className="border-t lg:border-t-0 lg:border-l-2 border-[#dfe2e5] flex flex-col justify-center items-center p-6 lg:p-10 bg-white relative">
      {!showLoginForm ? (
        <div className="w-full max-w-[380px] flex flex-col mt-8 lg:mt-0">
          <div className="flex items-center justify-between w-full mb-8">
            <h2 className="text-[22px] font-medium text-slate-900 tracking-tight">
              Zaloguj się do Facebooka
            </h2>
            <button
              className="text-black hover:bg-slate-100 p-2 rounded-full transition-colors cursor-pointer"
              aria-label="Ustawienia"
              onClick={() => setShowSettingsModal(true)}
            >
              <Settings size={24} />
            </button>
          </div>

          <div className="flex flex-col gap-1 mb-8">
            {profiles.map((profile) => (
              <button
                key={profile.id}
                onClick={() => handleProfileClick(profile)}
                className="flex items-center justify-between w-full p-2.5 hover:bg-slate-50 rounded-lg transition-colors group cursor-pointer"
              >
                <div className="flex items-center gap-4">
                  <div className="relative">
                    <img
                      src={profile.avatar}
                      alt={profile.name}
                      className="w-12 h-12 rounded-full object-cover border border-slate-200"
                    />
                    {profile.hasActiveSession && (
                      <span className="absolute bottom-0 right-0 block h-3.5 w-3.5 rounded-full bg-emerald-500 ring-2 ring-white" title="Aktywna sesja" />
                    )}
                  </div>
                  <span className="text-[15px] font-medium text-slate-900">{profile.name}</span>
                </div>
                <ChevronRight
                  size={24}
                  className="text-slate-400 group-hover:text-slate-600 transition-colors"
                />
              </button>
            ))}
          </div>

          <div className="w-full flex flex-col gap-4">
            <button
              className="w-full bg-white hover:bg-slate-50 text-slate-800 font-medium py-2.5 px-4 border border-slate-300 rounded-full transition-colors text-[14px] cursor-pointer"
              onClick={() => setShowLoginForm(true)}
            >
              Użyj innego profilu
            </button>

            <Link
              to="/register"
              className="w-full block"
            >
              <button className="w-full bg-white hover:bg-slate-50 text-[#1877F2] font-medium py-2.5 px-4 border border-[#1877F2] rounded-full transition-colors text-[14px] cursor-pointer">
                Utwórz nowe konto
              </button>
            </Link>
          </div>

          <div className="mt-8 flex items-center justify-center text-[15px] text-slate-800 font-normal">
            <MetaIcon
              size={20}
              className="text-[#1877F2] mr-1"
            />
            <span>Meta</span>
          </div>
        </div>
      ) : (
        <div className="w-full max-w-[380px] flex flex-col mt-8 lg:mt-0">
          <div className="flex items-center w-full mb-8 relative">
            {profiles.length > 0 && (
              <button
                className="absolute -left-3 text-black hover:bg-slate-100 p-2 rounded-full transition-colors cursor-pointer"
                onClick={() => setShowLoginForm(false)}
              >
                <ChevronLeft size={32} />
              </button>
            )}
            <h2 className={`text-[22px] font-medium text-slate-900 tracking-tight ${profiles.length > 0 ? 'ml-12' : ''}`}>
              Zaloguj się do Facebooka
            </h2>
          </div>

          <form
            className="w-full flex flex-col space-y-4"
            onSubmit={handleLogin}
          >
            {errorMessage && (
              <div className="p-3 text-sm text-red-600 bg-red-50 rounded-lg border border-red-200 text-left">
                {errorMessage}
              </div>
            )}

            <CustomInput
              id="email"
              ref={emailInputRef}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              type="text"
              label="Adres e-mail lub numer telefonu komórkowego"
              disableFocusColor={true}
            />

            <CustomInput
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              label="Hasło"
              disableFocusColor={true}
            />

            <AuthButton
              type="submit"
              disabled={isLoading}
            >
              {isLoading ? 'Logowanie...' : 'Zaloguj się'}
            </AuthButton>
          </form>

          <div className="w-full mt-5 text-center">
            <Link
              to="/findAccount"
              className="text-[15px] text-[#1877F2] font-medium hover:underline"
            >
              Nie pamiętasz hasła?
            </Link>
          </div>

          <div className="w-full mt-10">
            <Link
              to="/register"
              className="w-full block"
            >
              <AuthButton>
                Utwórz nowe konto
              </AuthButton>
            </Link>
          </div>

          <div className="mt-8 flex items-center justify-center text-[15px] text-slate-800 font-normal">
            <MetaIcon
              size={20}
              className="text-[#1877F2] mr-1"
            />
            <span>Meta</span>
          </div>
        </div>
      )}

      <LoginSettingsModal
        isOpen={showSettingsModal}
        onClose={() => setShowSettingsModal(false)}
        profiles={profiles}
        onRemoveProfile={handleRemoveProfile}
      />
    </section>
  )
}

export default LoginAuthPanel
