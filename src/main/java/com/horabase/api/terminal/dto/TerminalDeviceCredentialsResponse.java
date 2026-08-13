package com.horabase.api.terminal.dto;

public record TerminalDeviceCredentialsResponse(
        TerminalDeviceResponse terminal,
        String secret
) {
}
