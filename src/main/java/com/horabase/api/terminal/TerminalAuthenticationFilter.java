package com.horabase.api.terminal;

import com.horabase.api.common.error.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TerminalAuthenticationFilter extends OncePerRequestFilter {

    public static final String TERMINAL_REQUEST_ATTRIBUTE =
            "horabase.terminal";
    private static final String IDENTIFIER_HEADER = "X-Terminal-Id";
    private static final String SECRET_HEADER = "X-Terminal-Secret";
    private static final Pattern TERMINAL_PATH = Pattern.compile(
            "^/api/terminal/businesses/(\\d+)(?:/.*)?$"
    );

    private final TerminalDeviceRepository terminalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public TerminalAuthenticationFilter(
            TerminalDeviceRepository terminalRepository,
            PasswordEncoder passwordEncoder,
            JsonMapper jsonMapper,
            Clock clock
    ) {
        this.terminalRepository = terminalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !TERMINAL_PATH.matcher(request.getRequestURI()).matches();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String identifier = request.getHeader(IDENTIFIER_HEADER);
        String secret = request.getHeader(SECRET_HEADER);
        Matcher path = TERMINAL_PATH.matcher(request.getRequestURI());

        if (!path.matches()
                || identifier == null
                || secret == null
                || identifier.isBlank()
                || secret.isBlank()) {
            unauthorized(request, response);
            return;
        }

        Long businessId = Long.valueOf(path.group(1));
        TerminalDevice terminal = terminalRepository
                .findDetailedByIdentifier(
                        identifier.trim().toLowerCase(Locale.ROOT)
                )
                .filter(TerminalDevice::isActive)
                .filter(device -> device.getBusiness().isActive())
                .filter(device -> device.getBusiness().getId()
                        .equals(businessId))
                .filter(device -> passwordEncoder.matches(
                        secret,
                        device.getSecretHash()
                ))
                .orElse(null);

        if (terminal == null) {
            unauthorized(request, response);
            return;
        }

        terminal.setLastSeenAt(OffsetDateTime.now(clock));
        terminalRepository.save(terminal);
        request.setAttribute(TERMINAL_REQUEST_ATTRIBUTE, terminal.getId());
        filterChain.doFilter(request, response);
    }

    private void unauthorized(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "Credenciales de terminal inválidas",
                        request.getRequestURI()
                )
        );
    }
}
