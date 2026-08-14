package com.horabase.api.terminal;

import com.horabase.api.business.Business;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TerminalAuthenticationFilterUnitTests {

    private final TerminalDeviceRepository repository =
            mock(TerminalDeviceRepository.class);
    private final PasswordEncoder encoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final FilterChain chain = mock(FilterChain.class);
    private TerminalAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TerminalAuthenticationFilter(
                repository,
                encoder,
                JsonMapper.builder().build(),
                Clock.fixed(Instant.parse("2026-08-14T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void rejectsInvalidSecret() throws Exception {
        when(repository.findDetailedByIdentifier("caja-1"))
                .thenReturn(Optional.of(terminal(true)));

        MockHttpServletResponse response = execute("clave-incorrecta");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(
                "Credenciales de terminal inválidas"
        );
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsInactiveTerminalEvenWithValidSecret() throws Exception {
        when(repository.findDetailedByIdentifier("caja-1"))
                .thenReturn(Optional.of(terminal(false)));

        MockHttpServletResponse response = execute("secreto-valido");

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
        verify(repository, never()).save(any());
    }

    private MockHttpServletResponse execute(String secret) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/terminal/businesses/1/check-in"
        );
        request.addHeader("X-Terminal-Id", "CAJA-1");
        request.addHeader("X-Terminal-Secret", secret);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, chain);
        return response;
    }

    private TerminalDevice terminal(boolean active) {
        Business business = new Business();
        ReflectionTestUtils.setField(business, "id", 1L);
        business.setActive(true);

        TerminalDevice terminal = new TerminalDevice();
        ReflectionTestUtils.setField(terminal, "id", 2L);
        terminal.setBusiness(business);
        terminal.setIdentifier("caja-1");
        terminal.setSecretHash(encoder.encode("secreto-valido"));
        terminal.setActive(active);
        return terminal;
    }
}
