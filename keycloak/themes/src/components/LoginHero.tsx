import React from 'react'
import { Clock, Home, Star, Heart } from 'lucide-react'

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

export const LoginHero: React.FC = () => {
  return (
    <section className="relative flex flex-col justify-center items-center p-6 lg:p-0 bg-white min-h-[600px]">
      <div className="absolute top-8 left-8 lg:top-10 lg:left-12 z-20">
        <FacebookIcon
          size={48}
          className="text-[#1877F2]"
        />
      </div>

      <div className="absolute bottom-12 left-8 lg:bottom-20 lg:left-16 z-20 text-left">
        <h1 className="text-4xl md:text-5xl lg:text-[56px] font-bold tracking-tight text-slate-900 leading-[1.15]">
          Przeglądaj,<br />
          to, co <span className="text-[#1877F2]">lubisz.</span>
        </h1>
      </div>

      {/* Hero Canvas */}
      <div className="relative shrink-0 select-none w-[280px] h-[400px] mt-[-80px] lg:w-[320px] lg:h-[460px] lg:mt-0">
        <div className="absolute inset-0 rounded-[32px] overflow-hidden shadow-sm border border-slate-100 bg-slate-200 z-10">
          <img
            src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80"
            alt="Main User"
            className="w-full h-full object-cover"
          />
          <div className="absolute bottom-16 left-1/2 -translate-x-1/2 flex items-center gap-2 z-20">
            <div className="w-16 h-2 bg-white/40 border border-white rounded-full" />
            <div className="w-4 h-4 border-2 border-white rounded-full" />
            <div className="w-4 h-4 border-2 border-white rounded-full" />
          </div>
        </div>

        <div className="absolute top-10 -right-6 lg:-right-10 bg-[#7C3AED] text-white text-[13px] font-semibold px-3 py-1.5 rounded-full flex items-center gap-1.5 shadow-xl z-20">
          <Clock size={16} /> 16:45
        </div>

        <div className="absolute top-16 -left-16 lg:-left-24 rounded-[24px] bg-white p-2 shadow-2xl border border-slate-100 z-20 w-[144px] h-[192px] lg:w-[160px] lg:h-[224px]">
          <div className="relative w-full h-full rounded-xl overflow-hidden">
            <img
              src="https://images.unsplash.com/photo-1463936575829-25148e1db1b8?w=300&auto=format&fit=crop&q=80"
              alt="Plant"
              className="w-full h-full object-cover"
            />
            <div className="absolute top-2 left-2 bg-black/40 text-white p-1 rounded-lg backdrop-blur-sm">
              <Home size={14} />
            </div>
          </div>
        </div>

        <div className="absolute -top-4 left-0 lg:-left-4 text-5xl transform -rotate-12 drop-shadow-lg z-20 animate-bounce-slow">
          😆
        </div>

        <div className="absolute bottom-28 -right-6 lg:-right-8 bg-[#FF246A] text-white p-3 rounded-full shadow-2xl z-20 transform scale-110">
          <Heart size={28} fill="currentColor" />
        </div>

        <div className="absolute bottom-6 -left-12 lg:-left-20 w-44 lg:w-48 bg-white rounded-2xl shadow-2xl p-2 border border-slate-100 z-0">
          <div className="relative w-full h-28 lg:h-32 rounded-xl overflow-hidden mb-3">
            <img
              src="https://images.unsplash.com/photo-1564982009877-1c650907536a?w=300&auto=format&fit=crop&q=80"
              alt="Skatepark"
              className="w-full h-full object-cover"
            />
            <div className="absolute top-2 left-2 bg-[#1877F2] text-white p-1 rounded-md">
              <Star size={14} fill="currentColor" />
            </div>
          </div>
          <div className="px-2 pb-2 space-y-2">
            <div className="h-1.5 bg-slate-100 rounded-full w-full" />
            <div className="h-1.5 bg-slate-100 rounded-full w-2/3" />
            <div className="h-1.5 bg-slate-100 rounded-full w-4/5" />
          </div>
        </div>

        <div className="absolute -bottom-10 left-1/2 -translate-x-1/2 rounded-full border-4 border-[#1877F2] bg-white shadow-2xl z-30 w-[96px] h-[96px] lg:w-[112px] lg:h-[112px] p-[2px]">
          <img
            src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80"
            alt="Profile"
            className="w-full h-full object-cover rounded-full"
          />
        </div>
      </div>
    </section>
  )
}

export default LoginHero
