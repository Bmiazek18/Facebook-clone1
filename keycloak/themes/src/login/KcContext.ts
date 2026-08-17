/* eslint-disable @typescript-eslint/no-empty-object-type */
import type { ExtendKcContext } from "keycloakify/login";
import type { KcEnvName, ThemeName } from "../kc.gen";

export type KcContextExtension = {
    themeName: ThemeName;
    properties: Record<KcEnvName, string> & {};
    // NOTE: Here you can declare more properties to extend the KcContext
    // See: https://docs.keycloakify.dev/faq-and-help/some-values-you-need-are-missing-from-in-kccontext
};

export type KcContextExtensionPerPage = {
    "login-email-otp.ftl"?: {
        email?: string;
        maskedEmail?: string;
        deviceTrustEnabled?: boolean;
        deviceTrustPermanent?: boolean;
        trustDurationUnitKey?: string;
        trustDurationValue?: number;
        trustHideNumber?: boolean;
    };
};

export type KcContext = ExtendKcContext<KcContextExtension, KcContextExtensionPerPage>;

// "login-email-otp.ftl":
