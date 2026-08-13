package com.horabase.api.account;

public interface AccountAccessState {
    boolean getAccountActive();
    boolean getBusinessActive();
    boolean getMustChangePassword();
}
