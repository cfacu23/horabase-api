package com.horabase.api.auth;

import com.horabase.api.account.Account;

public interface PasswordResetEmailSender {

    void sendPasswordReset(Account account, String resetUrl);
}
