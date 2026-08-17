import { i18nBuilder } from "keycloakify/login";
import type { ThemeName } from "../kc.gen";

const { useI18n, ofTypeI18n } = i18nBuilder
    .withThemeName<ThemeName>()
    .withCustomTranslations({
        en: {
            "auth.register.title": "Create a new account",
            "auth.register.description": "It's quick and easy.",
            "auth.register.firstName": "First name",
            "auth.register.lastName": "Last name",
            "auth.register.birthdate": "Date of birth",
            "auth.register.day": "Day",
            "auth.register.month": "Month",
            "auth.register.year": "Year",
            "auth.register.gender": "Gender",
            "auth.register.emailOrPhone": "Mobile number or email",
            "auth.register.password": "New password",
            "auth.register.submit": "Sign Up",
            "dontAskForCodePermanently": "Remember this device",
            "dontAskForCodeFor": "Don't ask for code again on this device for {0} {1}",
            "doResendEmail": "Resend email",
            "loginEmailOtp": "OTP Code"
        },
        pl: {
            "auth.register.title": "Utwórz nowe konto",
            "auth.register.description": "To szybkie i proste.",
            "auth.register.firstName": "Imię",
            "auth.register.lastName": "Nazwisko",
            "auth.register.birthdate": "Data urodzenia",
            "auth.register.day": "Dzień",
            "auth.register.month": "Miesiąc",
            "auth.register.year": "Rok",
            "auth.register.gender": "Płeć",
            "auth.register.emailOrPhone": "Numer telefonu lub e-mail",
            "auth.register.password": "Nowe hasło",
            "auth.register.submit": "Zarejestruj się",
            "dontAskForCodePermanently": "Zapamiętaj to urządzenie",
            "dontAskForCodeFor": "Nie pytaj ponownie o kod na tym urządzeniu przez {0} {1}",
            "doResendEmail": "Wyślij e-mail ponownie",
            "loginEmailOtp": "Kod OTP"
        }
    })
    .build();

type I18n = typeof ofTypeI18n;
export { useI18n, type I18n };
