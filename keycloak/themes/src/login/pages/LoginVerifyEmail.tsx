import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import { ChevronLeft } from "lucide-react";

import AuthButton from "../../components/AuthButton";

export default function LoginVerifyEmail(props: PageProps<Extract<KcContext, { pageId: "login-verify-email.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, user } = kcContext;

    return (
        <div className="flex flex-col min-h-screen bg-white font-sans text-[#1c1e21] pt-10 px-4">
            <div className="w-full max-w-[440px] self-center flex flex-col flex-grow">
                <header className="mb-5 text-left">
                    <a
                        href={url.loginUrl}
                        className="text-gray-600 hover:text-black transition cursor-pointer"
                    >
                        <ChevronLeft size={24} />
                    </a>
                </header>

                <main className="w-full text-left">
                    {kcContext.message !== undefined && (
                        <div className={`mb-5 p-4 text-[14px] text-black bg-white rounded-2xl border shadow-sm flex items-start gap-3 ${
                            kcContext.message.type === "error" ? "border-red-200" : "border-green-200"
                        }`}>
                            <span dangerouslySetInnerHTML={{ __html: kcContext.message.summary }} />
                        </div>
                    )}

                    <h1 className="text-[20px] font-bold tracking-tight mb-2 text-[#0f1419]">
                        Potwierdź swoje konto
                    </h1>

                    <p className="text-[14px] leading-normal text-[#536471] mb-8">
                        Wysłaliśmy e-mail z linkiem aktywacyjnym na Twój adres
                        {user?.email && (
                            <>
                                : <span className="font-semibold text-black">{user.email}</span>
                            </>
                        )}
                        . Kliknij link w otrzymanej wiadomości, aby potwierdzić swoje konto.
                    </p>

                    <div className="space-y-4">
                        <a href={url.loginAction} className="w-full block">
                            <AuthButton type="button">
                                Wyślij e-mail ponownie
                            </AuthButton>
                        </a>

                        <a href={url.loginUrl} className="w-full block text-center mt-2">
                            <button
                                type="button"
                                className="w-full bg-white hover:bg-gray-50 text-black border border-gray-300 font-semibold py-3 px-4 rounded-full text-[15px] transition duration-150 cursor-pointer"
                            >
                                Wróć do logowania
                            </button>
                        </a>
                    </div>
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
    );
}
