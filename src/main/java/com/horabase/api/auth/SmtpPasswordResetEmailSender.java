package com.horabase.api.auth;

import com.horabase.api.account.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetEmailSender
        implements PasswordResetEmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpPasswordResetEmailSender(
            JavaMailSender mailSender,
            @Value("${horabase.mail.from:no-reply@horabase.local}")
            String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendPasswordReset(Account account, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(account.getEmail());
        message.setSubject("Recuperación de contraseña de HoraBase");
        message.setText("""
                Se solicitó restablecer la contraseña de tu cuenta HoraBase.

                Usá el siguiente enlace antes de que expire:
                %s

                Si no realizaste esta solicitud, ignorá este mensaje.
                """.formatted(resetUrl));
        mailSender.send(message);
    }
}
