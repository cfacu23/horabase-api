package com.horabase.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTests {

    @Test
    void configuresOnlyDeclaredOriginsAndRequiredHeaders() {
        var source = new CorsConfig().corsConfigurationSource(
                "https://app.horabase.test, https://admin.horabase.test"
        );
        var request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        var configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly(
                "https://app.horabase.test",
                "https://admin.horabase.test"
        );
        assertThat(configuration.getAllowedOrigins()).doesNotContain("*");
        assertThat(configuration.getAllowedHeaders()).contains(
                "Authorization", "X-Terminal-Id", "X-Terminal-Secret"
        );
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
