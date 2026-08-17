import { useState, useRef, useEffect } from "react";
import { Settings, ChevronRight, ChevronLeft, Infinity as MetaIcon } from "lucide-react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";

import LoginHero from "../../components/LoginHero";
import CustomInput from "../../components/CustomInput";
import type { CustomInputRef } from "../../components/CustomInput";
import AuthButton from "../../components/AuthButton";
import LoginSettingsModal from "../../components/LoginSettingsModal";
import { type UserProfile, encryptAndSaveProfiles, loadAndDecryptProfiles } from "../cryptoStorage";



const getNameFromUsername = (uname: string) => {
    const part = uname.includes("@") ? uname.split("@")[0] : uname;
    return part
        .split(/[._-]/)
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
};

export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, realm, login } = kcContext;

    const [profiles, setProfiles] = useState<UserProfile[]>([]);
    const [showLoginForm, setShowLoginForm] = useState(true);

    const [showSettingsModal, setShowSettingsModal] = useState(false);
    const [username, setUsername] = useState(() => {
        if (typeof window !== "undefined") {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get("login_hint") || login?.username || "";
        }
        return login?.username ?? "";
    });
    const [password, setPassword] = useState("");
    const emailInputRef = useRef<CustomInputRef | null>(null);
    const formRef = useRef<HTMLFormElement | null>(null);

    const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);

    // Sync profiles and session cookie status
    useEffect(() => {
        if (typeof window === "undefined") return;

        async function initProfiles() {
            const errorMsg = kcContext.message;
            const hasSessionCookie = document.cookie.split(';').some(item => item.trim().startsWith('KEYCLOAK_SESSION='));
            const pending = localStorage.getItem("pending_login_username");
            
            let profilesList = await loadAndDecryptProfiles();

            if (errorMsg && errorMsg.type === "error") {
                // Last attempt failed, remove from pending/profiles
                if (pending) {
                    profilesList = profilesList.filter((p) => p.username.toLowerCase() !== pending.toLowerCase());
                    await encryptAndSaveProfiles(profilesList);
                    localStorage.removeItem("pending_login_username");
                }
            } else if (pending) {
                // Successful login detected (since page loaded without error, meaning previous submit succeeded)
                profilesList = profilesList.filter((p) => p.username.toLowerCase() !== pending.toLowerCase());
                const newProfile: UserProfile = {
                    id: Date.now(),
                    name: getNameFromUsername(pending),
                    username: pending,
                    avatar: `https://images.unsplash.com/photo-1640951613773-54706e06851d?w=100&auto=format&fit=crop&q=60`,
                    hasActiveSession: true,
                    lastLogin: Date.now()
                };
                profilesList.unshift(newProfile);
                await encryptAndSaveProfiles(profilesList);
                localStorage.removeItem("pending_login_username");
            }

            // Check if session cookies are present. If not, set hasActiveSession to false for all.
            if (!hasSessionCookie) {
                profilesList = profilesList.map((p) => ({ ...p, hasActiveSession: false }));
                await encryptAndSaveProfiles(profilesList);
            }

            setProfiles(profilesList);

            // Decide whether to show login form
            const hasError = kcContext.message !== undefined && kcContext.message.type === "error";
            const urlParams = new URLSearchParams(window.location.search);
            const isDeviceLoginAttempt = urlParams.get("device_login") === "true" || urlParams.get("login_hint") || login?.username;
            
            if (hasError || isDeviceLoginAttempt || profilesList.length === 0) {
                setShowLoginForm(true);
            } else {
                setShowLoginForm(false);
            }
        }

        initProfiles();
    }, [kcContext.message]);

    // Focus email input when login form is opened
    useEffect(() => {
        if (showLoginForm) {
            setTimeout(() => {
                emailInputRef.current?.focus();
            }, 50);
        }
    }, [showLoginForm]);

    const handleRemoveProfile = async (id: number) => {
        const updated = profiles.filter((p) => p.id !== id);
        setProfiles(updated);
        await encryptAndSaveProfiles(updated);
        
        // If no profiles left, switch to login form automatically
        if (updated.length === 0) {
            setShowLoginForm(true);
        }
    };

    const handleProfileClick = (profile: UserProfile) => {
        // Attempt Facebook-style one-click login by sending device_login=true and login_hint.
        // If the browser has a valid HTTP-only device_auth cookie matching this username, Keycloak logs them in.
        // If the cookie is expired/missing, Keycloak falls back to the password form with login_hint pre-filled.
        const urlParams = new URLSearchParams(window.location.search);
        urlParams.delete("prompt");
        urlParams.set("login_hint", profile.username);
        urlParams.set("device_login", "true");
        window.location.search = urlParams.toString();
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsLoginButtonDisabled(true);
        
        // Save the username as pending login
        localStorage.setItem("pending_login_username", username);

        // Add to recent profiles immediately as active
        try {
            let profilesList = await loadAndDecryptProfiles();
            profilesList = profilesList.filter((p: any) => p.username.toLowerCase() !== username.toLowerCase());
            const newProfile = {
                id: Date.now(),
                name: getNameFromUsername(username),
                username: username,
                avatar: `https://images.unsplash.com/photo-1640951613773-54706e06851d?w=100&auto=format&fit=crop&q=60`,
                hasActiveSession: true,
                lastLogin: Date.now()
            };
            profilesList.unshift(newProfile);
            await encryptAndSaveProfiles(profilesList);
        } catch (err) {
            console.error("Error saving profile", err);
        }

        // Programmatically submit the form after encryption completes
        formRef.current?.submit();
    };

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

                <div className="hidden lg:block">
    <LoginHero />
</div>

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

                                {realm.registrationAllowed && (
                                    <a href={url.registrationUrl} className="w-full block">
                                        <button className="w-full bg-white hover:bg-slate-50 text-[#1877F2] font-medium py-2.5 px-4 border border-[#1877F2] rounded-full transition-colors text-[14px] cursor-pointer">
                                            Utwórz nowe konto
                                        </button>
                                    </a>
                                )}
                            </div>

                            <div className="mt-8 flex items-center justify-center text-[15px] text-slate-800 font-normal">
                                <MetaIcon size={20} className="text-[#1877F2] mr-1" />
                                <span>Meta</span>
                            </div>
                        </div>
                    ) : (
                        <div className="w-full max-w-[380px] flex flex-col mt-8 lg:mt-0">
                            <div className="flex items-center w-full mb-8 relative">
                                <button
                                    className="absolute -left-3 text-black hover:bg-slate-100 p-2 rounded-full transition-colors cursor-pointer"
                                    onClick={() => setShowLoginForm(false)}
                                >
                                    <ChevronLeft size={32} />
                                </button>
                                <h2 className="text-[22px] font-medium text-slate-900 ml-12 tracking-tight">
                                    Zaloguj się do Facebooka
                                </h2>
                            </div>

                            <form
                                id="kc-form-login"
                                ref={formRef}
                                className="w-full flex flex-col space-y-4"
                                action={url.loginAction}
                                method="post"
                                onSubmit={handleSubmit}
                            >
                                {/* Keycloak error/info messages */}
                                {kcContext.message !== undefined && (
                                    <div className={`p-3 text-sm rounded-lg border text-left ${
                                        kcContext.message.type === "error"
                                            ? "text-red-600 bg-red-50 border-red-200"
                                            : "text-blue-600 bg-blue-50 border-blue-200"
                                    }`}>
                                        <span dangerouslySetInnerHTML={{ __html: kcContext.message.summary }} />
                                    </div>
                                )}

                                <CustomInput
                                    id="username"
                                    name="username"
                                    ref={emailInputRef}
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    type="text"
                                    label="Adres e-mail lub numer telefonu komórkowego"
                                    disableFocusColor={true}
                                />

                                <CustomInput
                                    id="password"
                                    name="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    type="password"
                                    label="Hasło"
                                    disableFocusColor={true}
                                />

                                {realm.rememberMe && (
                                    <div className="flex items-center gap-2 text-left">
                                        <input
                                            id="rememberMe"
                                            name="rememberMe"
                                            type="checkbox"
                                            defaultChecked={!!login?.rememberMe}
                                            className="rounded border-gray-300 text-primary focus:ring-primary"
                                        />
                                        <label htmlFor="rememberMe" className="text-sm text-slate-600">
                                            Zapamiętaj mnie
                                        </label>
                                    </div>
                                )}

                                <AuthButton
                                    type="submit"
                                    name="login"
                                    id="kc-login"
                                    disabled={isLoginButtonDisabled}
                                >
                                    {isLoginButtonDisabled ? "Logowanie..." : "Zaloguj się"}
                                </AuthButton>
                            </form>

                            {realm.resetPasswordAllowed && (
                                <div className="w-full mt-5 text-center">
                                    <a
                                        href={url.loginResetCredentialsUrl}
                                        className="text-[15px] text-[#1877F2] font-medium hover:underline"
                                    >
                                        Nie pamiętasz hasła?
                                    </a>
                                </div>
                            )}

                            {realm.registrationAllowed && (
                                <div className="w-full mt-10">
                                    <a href={url.registrationUrl} className="w-full block">
                                        <AuthButton variant="secondary">
                                            Utwórz nowe konto
                                        </AuthButton>
                                    </a>
                                </div>
                            )}

                            <div className="mt-8 flex items-center justify-center text-[15px] text-slate-800 font-normal">
                                <MetaIcon size={20} className="text-[#1877F2] mr-1" />
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
    );
}
