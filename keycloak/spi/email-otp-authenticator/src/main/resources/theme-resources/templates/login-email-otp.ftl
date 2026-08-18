<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email-otp'); section>
<!-- template: login-email-otp.ftl -->
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
        <form id="kc-otp-login-form" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <#if maskedEmail??>
                    <div class="otp-email-info" style="margin-bottom: 15px; font-size: 0.95em; color: #4b5563; line-height: 1.5;">
                        ${msg("emailSentTo", maskedEmail)} 
                        <a href="#" id="change-email-btn" style="margin-left: 4px; font-weight: 600; text-decoration: underline; color: #2563eb; cursor: pointer;">${msg("changeEmailLink")}</a>
                    </div>
                </#if>

                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="email-otp" class="${properties.kcLabelClass!}">${msg("loginEmailOtp")}</label>
                </div>

                <div class="${properties.kcInputWrapperClass!}">
                    <input id="email-otp" name="email-otp" autocomplete="one-time-code" type="text" class="${properties.kcInputClass!}" autofocus=true aria-invalid="<#if messagesPerField.existsError('email-otp')>true</#if>" dir="ltr" />

                    <#if messagesPerField.existsError('email-otp')>
                        <span id="input-error-email-otp-code" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('email-otp'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <#if deviceTrustEnabled?? && deviceTrustEnabled>
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcInputWrapperClass!}">
                    <div class="${properties.kcCheckboxInputClass!}">
                        <input type="checkbox" id="trust-device" name="trust-device" value="true" />
                        <label for="trust-device" class="${properties.kcCheckboxLabelClass!}">
                            <#if deviceTrustPermanent?? && deviceTrustPermanent>
                                ${msg("dontAskForCodePermanently")}
                            <#elseif trustDurationUnitKey??>
                                <#assign unitStr = msg(trustDurationUnitKey)>
                                <#if trustHideNumber?? && trustHideNumber>
                                    ${msg("dontAskForCodeFor", "", unitStr)?trim}
                                  <#else>
                                    ${msg("dontAskForCodeFor", trustDurationValue!1, unitStr)}
                                </#if>
                            <#else>
                                ${msg("dontAskForCodePermanently")}
                            </#if>
                        </label>
                    </div>
                </div>
            </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <button
                            class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                            name="login"
                            id="kc-login"
                            type="submit"
                        >${kcSanitize(msg("doLogIn"))?no_esc}</button>

                        <button
                            class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                            name="resend-email"
                            id="kc-resend-email"
                            type="submit"
                        >${kcSanitize(msg("doResendEmail"))?no_esc}</button>

                        <button
                            class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                            name="cancel-login"
                            id="kc-cancel-login"
                            type="submit"
                            style="margin-top: 8px;"
                        >${kcSanitize(msg("cancelLogin"))?no_esc}</button>
                    </div>
                </div>
            </div>
        </form>

        <!-- Change Email Modal Backdrop & Window -->
        <div id="change-email-modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000; align-items: center; justify-content: center;">
            <div style="background: #ffffff; border-radius: 8px; padding: 24px; max-width: 400px; width: 90%; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1); color: #1f2937; text-align: left; box-sizing: border-box; border: 1px solid #e5e7eb;">
                <h3 style="margin-top: 0; margin-bottom: 16px; font-size: 1.25rem; font-weight: 700; color: #111827; border-bottom: 1px solid #f3f4f6; padding-bottom: 12px;">${msg("changeEmailTitle")}</h3>
                <form id="kc-change-email-form" action="${url.loginAction}" method="post">
                    <div class="${properties.kcFormGroupClass!}" style="margin-bottom: 20px;">
                        <label for="new-email" class="${properties.kcLabelClass!}" style="display: block; margin-bottom: 8px; font-weight: 600; color: #374151;">${msg("changeEmailLabel")}</label>
                        <input id="new-email" name="new-email" type="email" class="${properties.kcInputClass!}" required style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; font-size: 1rem;" value="${email!""}" />
                    </div>
                    <div style="display: flex; justify-content: flex-end; gap: 12px;">
                        <button type="button" id="close-change-email-modal" class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!}" style="background: #f3f4f6; color: #4b5563; border: 1px solid #d1d5db; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.95rem;">${msg("changeEmailCancel")}</button>
                        <button type="submit" name="change-email" value="true" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!}" style="background: #2563eb; color: #ffffff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.95rem;">${msg("changeEmailSubmit")}</button>
                    </div>
                </form>
            </div>
        </div>

        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var modal = document.getElementById("change-email-modal");
                var btn = document.getElementById("change-email-btn");
                var closeBtn = document.getElementById("close-change-email-modal");

                if (btn && modal) {
                    btn.addEventListener("click", function(e) {
                        e.preventDefault();
                        modal.style.display = "flex";
                        var emailInput = document.getElementById("new-email");
                        if (emailInput) {
                            emailInput.focus();
                        }
                    });
                }

                if (closeBtn && modal) {
                    closeBtn.addEventListener("click", function() {
                        modal.style.display = "none";
                    });
                }

                if (modal) {
                    modal.addEventListener("click", function(e) {
                        if (e.target === modal) {
                            modal.style.display = "none";
                        }
                    });
                }
            });
        </script>
    </#if>
</@layout.registrationLayout>
