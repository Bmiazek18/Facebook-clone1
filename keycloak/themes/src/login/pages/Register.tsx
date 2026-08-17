import { useState, useLayoutEffect } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import { ArrowLeft, Eye, EyeOff, HelpCircle, KeyRound } from "lucide-react";

import CustomInput from "../../components/CustomInput";
import CustomDropdown from "../../components/CustomDropdown";
import AuthButton from "../../components/AuthButton";

export default function Register(props: PageProps<Extract<KcContext, { pageId: "register.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, recaptchaRequired, recaptchaVisible, recaptchaSiteKey, recaptchaAction } = kcContext;

    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [emailOrPhone, setEmailOrPhone] = useState("");
    const [day, setDay] = useState("");
    const [month, setMonth] = useState("");
    const [year, setYear] = useState("");
    const [gender, setGender] = useState("");
    const [password, setPassword] = useState("");

    const [showPassword, setShowPassword] = useState(false);
    const [showGenderTooltip, setShowGenderTooltip] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const genderOptions = [
        { id: "female", title: "Kobieta" },
        { id: "male", title: "Mężczyzna" },
        { id: "custom", title: "Niestandardowa" }
    ];

    const dayOptions = Array.from({ length: 31 }, (_, i) => ({
        id: String(i + 1),
        title: String(i + 1)
    }));

    const monthNames = ["Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"];
    const monthOptions = monthNames.map((m, i) => ({
        id: String(i + 1),
        title: m
    }));

    const currentYear = new Date().getFullYear();
    const yearOptions = Array.from({ length: 100 }, (_, i) => ({
        id: String(currentYear - i),
        title: String(currentYear - i)
    }));

    useLayoutEffect(() => {
        (window as any)["onSubmitRecaptcha"] = () => {
            // @ts-expect-error
            document.getElementById("kc-register-form").requestSubmit();
        };

        return () => {
            delete (window as any)["onSubmitRecaptcha"];
        };
    }, []);

    const isFormValid =
        firstName.trim().length > 0 &&
        lastName.trim().length > 0 &&
        emailOrPhone.trim().length > 0 &&
        day !== "" &&
        month !== "" &&
        year !== "" &&
        gender !== "" &&
        password.trim().length > 0;

    return (
        <div className="min-h-screen bg-theme-bg-secondary flex items-center justify-center p-4 font-sans text-[#1c1e21]">
            <div className="w-full max-w-[600px] p-4 sm:p-5">
                <div className="flex items-center justify-between mb-4">
                    <a
                        href={url.loginUrl}
                        className="text-[#606770] hover:bg-gray-100 p-2 rounded-full transition-colors focus:outline-none cursor-pointer"
                    >
                        <ArrowLeft size={20} />
                    </a>
                    <img
                        src="https://upload.wikimedia.org/wikipedia/commons/7/7b/Meta_Platforms_Inc._logo.svg"
                        alt="Meta"
                        className="h-4"
                    />
                    <div className="w-9" />
                </div>

                <h1 className="text-[22px] sm:text-2xl font-bold mb-1.5 text-center sm:text-left">
                    Utwórz konto
                </h1>
                <p className="text-[14px] sm:text-[15px] text-[#606770] mb-5 text-center sm:text-left leading-snug">
                    Szybkie i proste.
                </p>

                <form
                    id="kc-register-form"
                    action={url.registrationAction}
                    method="post"
                    className="space-y-4 text-left"
                    onSubmit={() => {
                        setIsSubmitting(true);
                        return true;
                    }}
                >
                    {kcContext.message !== undefined && (
                        <div className={`p-3 text-sm rounded-lg border text-left mb-4 ${
                            kcContext.message.type === "error"
                                ? "text-red-600 bg-red-50 border-red-200"
                                : "text-blue-600 bg-blue-50 border-blue-200"
                        }`}>
                            <span dangerouslySetInnerHTML={{ __html: kcContext.message.summary }} />
                        </div>
                    )}

                    {/* Hidden inputs to bind custom React component values to native HTML form submission */}
                    <input type="hidden" name="email" value={emailOrPhone} />
                    <input type="hidden" name="username" value={emailOrPhone} />
                    <input type="hidden" name="password" value={password} />
                    <input type="hidden" name="password-confirm" value={password} />
                    <input type="hidden" name="attributes.birthDay" value={day} />
                    <input type="hidden" name="attributes.birthMonth" value={month} />
                    <input type="hidden" name="attributes.birthYear" value={year} />
                    <input type="hidden" name="attributes.gender" value={gender} />

                    <fieldset className="mb-3">
                        <legend className="text-[15px] font-semibold mb-2 text-[#1c1e21] text-left">
                            Imię i nazwisko
                        </legend>
                        <div className="flex gap-2.5">
                            <div className="flex-1">
                                <CustomInput
                                    id="firstName"
                                    name="firstName"
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}
                                    label="Imię"
                                    autoFocus
                                />
                            </div>
                            <div className="flex-1">
                                <CustomInput
                                    id="lastName"
                                    name="lastName"
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}
                                    label="Nazwisko"
                                />
                            </div>
                        </div>
                    </fieldset>

                    <fieldset className="mb-3">
                        <legend className="text-[15px] font-semibold mb-2 flex items-center gap-1 text-left text-[#1c1e21]">
                            Data urodzenia <HelpCircle size={16} className="text-[#606770] cursor-help" />
                        </legend>
                        <div className="flex gap-2.5">
                            <div className="flex-1">
                                <CustomDropdown
                                    value={day}
                                    onChange={setDay}
                                    label="Dzień"
                                    options={dayOptions}
                                />
                            </div>
                            <div className="flex-1">
                                <CustomDropdown
                                    value={month}
                                    onChange={setMonth}
                                    label="Miesiąc"
                                    options={monthOptions}
                                />
                            </div>
                            <div className="flex-[1.2]">
                                <CustomDropdown
                                    value={year}
                                    onChange={setYear}
                                    label="Rok"
                                    options={yearOptions}
                                />
                            </div>
                        </div>
                    </fieldset>

                    <fieldset className="mb-3">
                        <legend className="text-[15px] font-semibold mb-2 flex items-center gap-1 text-left text-[#1c1e21]">
                            Płeć
                            <div className="relative inline-block">
                                <button
                                    type="button"
                                    onClick={() => setShowGenderTooltip(!showGenderTooltip)}
                                    className="focus:outline-none flex items-center justify-center cursor-pointer"
                                >
                                    <HelpCircle
                                        size={16}
                                        className="text-[#606770] hover:text-[#1c1e21] transition-colors"
                                    />
                                </button>
                                {showGenderTooltip && (
                                    <div className="absolute left-0 top-6 z-50 p-4 w-[340px] text-[13px] text-[#1c1e21] leading-snug bg-white rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] border border-gray-100 text-left">
                                        Później możesz zmienić widoczność informacji dotyczących płci w profilu. Wybierz
                                        opcję Niestandardowa, aby wybrać inną płeć lub nie podawać tych informacji.
                                    </div>
                                )}
                            </div>
                        </legend>

                        <CustomDropdown
                            value={gender}
                            onChange={setGender}
                            label="Płeć"
                            options={genderOptions}
                        />
                    </fieldset>

                    <fieldset className="mb-3">
                        <legend className="text-[15px] font-semibold mb-2 text-[#1c1e21] text-left">
                            Adres e-mail lub numer telefonu komórkowego
                        </legend>
                        <CustomInput
                            id="emailOrPhone"
                            value={emailOrPhone}
                            onChange={(e) => setEmailOrPhone(e.target.value)}
                            label="Adres e-mail lub numer telefonu komórkowego"
                        />
                        <p className="text-[12px] text-[#606770] mt-1.5 leading-normal text-left">
                            Możesz otrzymywać od nas powiadomienia.{" "}
                            <a href="#" className="text-[#1877f2] hover:underline">
                                Dowiedz się, dlaczego prosimy o dane kontaktowe
                            </a>
                        </p>
                    </fieldset>

                    <fieldset className="mb-5">
                        <legend className="text-[15px] font-semibold mb-2 text-[#1c1e21] text-left">
                            Hasło
                        </legend>
                        <CustomInput
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            label="Nowe hasło"
                            type={showPassword ? "text" : "password"}
                            icon={
                                <div className="flex items-center gap-2.5 text-[#606770]">
                                    <KeyRound size={18} />
                                    <button
                                        type="button"
                                        className="focus:outline-none hover:text-[#1c1e21] transition-colors cursor-pointer"
                                        onClick={() => setShowPassword(!showPassword)}
                                    >
                                        {showPassword ? <Eye size={18} /> : <EyeOff size={18} />}
                                    </button>
                                </div>
                            }
                        />
                    </fieldset>

                    <div className="text-[11px] sm:text-[11.5px] text-[#606770] mb-6 leading-normal space-y-2.5 text-left">
                        <p>
                            Osoby korzystające z naszej usługi mogły przesłać Twoje dane kontaktowe do platformy
                            Facebook.{" "}
                            <a href="#" className="text-[#1877f2] hover:underline">
                                Dowiedz się więcej
                            </a>
                            .
                        </p>
                        <p>
                            Klikając przycisk Prześlij, akceptujesz utworzenie konta i{" "}
                            <a href="#" className="text-[#1877f2] hover:underline">
                                Regulamin
                            </a>{" "}
                            Facebooka. Informacje o tym, jak zbieramy, wykorzystujemy i udostępniamy Twoje dane,
                            zawierają nasze{" "}
                            <a href="#" className="text-[#1877f2] hover:underline">
                                Zasady ochrony prywatności
                            </a>
                            . O wykorzystaniu plików cookie i podobnych technologii informują{" "}
                            <a href="#" className="text-[#1877f2] hover:underline">
                                Zasady dotyczące plików cookie
                            </a>
                            .
                        </p>
                    </div>

                    <div className="flex flex-col gap-2.5 w-full">
                        {recaptchaRequired && !recaptchaVisible && recaptchaAction !== undefined ? (
                            <button
                                className="w-full font-medium py-3 px-4 rounded-full text-[15px] bg-primary hover:bg-primary-600 text-white transition-colors cursor-pointer g-recaptcha"
                                data-sitekey={recaptchaSiteKey}
                                data-callback="onSubmitRecaptcha"
                                data-action={recaptchaAction}
                                type="submit"
                            >
                                Zarejestruj się
                            </button>
                        ) : (
                            <AuthButton
                                type="submit"
                                disabled={!isFormValid || isSubmitting}
                            >
                                {isSubmitting ? "Tworzenie konta..." : "Zarejestruj się"}
                            </AuthButton>
                        )}

                        <a href={url.loginUrl} className="w-full block text-center mt-2">
                            <button
                                type="button"
                                className="w-full py-2.5 sm:py-3 px-4 rounded-full border border-[#bcc0c4] text-[#4b4f56] font-semibold text-[14px] sm:text-[15px] bg-white hover:bg-gray-50 active:bg-gray-100 transition-colors focus:outline-none cursor-pointer"
                            >
                                Mam już konto
                            </button>
                        </a>
                    </div>
                </form>
            </div>
        </div>
    );
}
