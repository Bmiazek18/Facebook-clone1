<#ftl output_format="HTML">
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${kcSanitize(msg("emailOtpSubject"))}</title>
</head>
<body style="font-family: Helvetica, Arial, sans-serif; color: #1c1e21; background-color: #ffffff; margin: 0; padding: 20px;">

    <!-- Nagłówek z logo i nazwa użytkownika -->
    <table width="100%" cellpadding="0" cellspacing="0" style="border-bottom: 1px solid #dadde1; padding-bottom: 12px; margin-bottom: 20px;">
        <tr>
            <td style="font-size: 28px; font-weight: bold; color: #1877f2; font-family: sans-serif;">
                facebook
            </td>
            <td align="right" style="font-size: 14px; color: #4b4f56;">
                ${user.username!''}
            </td>
        </tr>
    </table>

    <!-- Główna treść -->
    <div style="max-width: 600px; margin: 0 auto;">
        <h2 style="font-size: 22px; font-weight: bold; color: #1c1e21; margin-bottom: 20px;">
            ${kcSanitize(msg("emailOtpHeaderTitle"))}
        </h2>

        <p style="font-size: 16px; line-height: 20px; color: #1c1e21; margin-bottom: 16px;">
            ${kcSanitize(msg("emailOtpGreeting", user.firstName!''))}
        </p>

        <p style="font-size: 16px; line-height: 20px; color: #1c1e21; margin-bottom: 24px;">
            ${kcSanitize(msg("emailOtpBodyInstruction"))}
        </p>

        <!-- Pudełko z kodem OTP -->
        <div style="background-color: #f5f6f7; border: 1px solid #ccd0d5; border-radius: 6px; padding: 20px; text-align: center; margin-bottom: 24px;">
            <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1c1e21;">
                ${otp}
            </span>
        </div>

        <p style="font-size: 12px; color: #606770; text-align: center; margin-bottom: 30px;">
            ${kcSanitize(msg("emailOtpDoNotShare"))}
        </p>

        <!-- Sekcja ostrzegawcza: Jeśli ktoś pyta o kod -->
        <div style="margin-bottom: 20px;">
            <p style="font-size: 14px; font-weight: bold; color: #1c1e21; margin-bottom: 6px;">
                ${kcSanitize(msg("emailOtpWarningTitle"))}
            </p>
            <p style="font-size: 14px; line-height: 18px; color: #4b4f56; margin: 0;">
                ${kcSanitize(msg("emailOtpWarningBody"))}
            </p>
        </div>

        <!-- Sekcja ostrzegawcza: Jeśli nie prosiłeś o kod -->
        <div style="border-top: 1px solid #dadde1; padding-top: 20px; margin-top: 20px;">
            <p style="font-size: 14px; font-weight: bold; color: #1c1e21; margin-bottom: 6px;">
                ${kcSanitize(msg("emailOtpNotRequestedTitle"))}
            </p>
            <p style="font-size: 14px; line-height: 18px; color: #4b4f56; margin: 0;">
                ${kcSanitize(msg("emailOtpNotRequestedBody"))}
            </p>
        </div>
    </div>

</body>
</html>
