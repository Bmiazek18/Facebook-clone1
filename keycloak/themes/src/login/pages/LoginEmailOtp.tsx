import { useState, useRef } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import { ChevronLeft } from "lucide-react";

import AuthButton from "../../components/AuthButton";
import CustomInput from "../../components/CustomInput";

export default function LoginEmailOtp(props: PageProps<Extract<KcContext, { pageId: "login-email-otp.ftl" }>, I18n>) {
    const { kcContext, i18n } = props;
    const { msgStr } = i18n;
    const { url, email, deviceTrustEnabled, deviceTrustPermanent, trustDurationUnitKey, trustDurationValue } = kcContext;
    const [otp, setOtp] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);
    const resendButtonRef = useRef<HTMLButtonElement | null>(null);

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
                        Wysłaliśmy jednorazowy kod bezpieczeństwa (OTP) na Twój adres e-mail
                        {email ? (
                            <>
                                : <span className="font-semibold text-black">{email}</span>
                            </>
                        ) : null}
                        . Wprowadź go poniżej, aby potwierdzić swoje konto.
                    </p>

                    <form action={url.loginAction} method="post" className="space-y-4">
                        <CustomInput
                            id="email-otp"
                            name="email-otp"
                            label={msgStr("loginEmailOtp")}
                            type="text"
                            value={otp}
                            onChange={(e) => setOtp(e.target.value)}
                            autoComplete="one-time-code"
                            required
                            autoFocus
                        />

                        {deviceTrustEnabled && (
                            <div className="flex items-center gap-2 py-2">
                                <input
                                    type="checkbox"
                                    id="trust-device"
                                    name="trust-device"
                                    value="true"
                                    className="w-4 h-4 text-[#1877f2] border-gray-300 rounded focus:ring-[#1877f2] cursor-pointer"
                                />
                                <label htmlFor="trust-device" className="text-[14px] text-[#536471] cursor-pointer select-none">
                                    {deviceTrustPermanent
                                        ? msgStr("dontAskForCodePermanently")
                                        : trustDurationUnitKey
                                            ? msgStr("dontAskForCodeFor", String(trustDurationValue ?? 1), msgStr(trustDurationUnitKey as any))
                                            : msgStr("dontAskForCodePermanently")}
                                </label>
                            </div>
                        )}

                        <button
                            type="submit"
                            name="resend-email"
                            value="true"
                            ref={resendButtonRef}
                            className="hidden"
                        />

                        <div className="space-y-3 pt-2">
                            <AuthButton type="submit" name="login" value="true">
                                {msgStr("doLogIn")}
                            </AuthButton>

                            <button
                                type="button"
                                onClick={() => setIsModalOpen(true)}
                                className="w-full bg-white hover:bg-gray-50 text-black border border-gray-300 font-semibold py-3 px-4 rounded-full text-[15px] transition duration-150 cursor-pointer"
                            >
                                {msgStr("doResendEmail")}
                            </button>
                        </div>
                    </form>

                    {isModalOpen && (
                        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
                            <div className="bg-white rounded-3xl max-w-[360px] w-full p-6 shadow-2xl border border-gray-100 flex flex-col items-center text-center animate-scale-up">
                                <h3 className="text-[18px] font-bold text-slate-900 mb-2">
                                    Problem z otrzymaniem kodu?
                                </h3>
                                <p className="text-[14px] text-slate-500 mb-6">
                                    Wybierz jedną z opcji poniżej, aby kontynuować proces logowania.
                                </p>

                                <div className="w-full flex flex-col gap-2.5">
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setIsModalOpen(false);
                                            resendButtonRef.current?.click();
                                        }}
                                        className="w-full bg-[#1877f2] hover:bg-[#166fe5] text-white font-semibold py-3 px-4 rounded-full text-[15px] transition duration-150 cursor-pointer"
                                    >
                                        Wyślij jeszcze raz
                                    </button>

                                    <a
                                        href={url.loginRestartFlowUrl || url.loginUrl}
                                        className="w-full text-center block"
                                    >
                                        <button
                                            type="button"
                                            className="w-full bg-slate-50 hover:bg-slate-100 text-slate-800 border border-slate-200 font-semibold py-3 px-4 rounded-full text-[15px] transition duration-150 cursor-pointer"
                                        >
                                            Wprowadź nowy email
                                        </button>
                                    </a>

                                    <a
                                        href={url.loginRestartFlowUrl || url.loginUrl}
                                        className="w-full text-center block"
                                    >
                                        <button
                                            type="button"
                                            className="w-full bg-white hover:bg-red-50 text-red-600 border border-red-200 font-semibold py-3 px-4 rounded-full text-[15px] transition duration-150 cursor-pointer"
                                        >
                                            Wyloguj się
                                        </button>
                                    </a>

                                    <button
                                        type="button"
                                        onClick={() => setIsModalOpen(false)}
                                        className="w-full bg-white hover:bg-slate-50 text-slate-500 font-medium py-2 px-4 rounded-full text-[14px] transition duration-150 cursor-pointer mt-1"
                                    >
                                        Anuluj
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
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
