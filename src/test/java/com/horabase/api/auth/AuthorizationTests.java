package com.horabase.api.auth;

import com.horabase.api.account.AccountRepository;
import com.horabase.api.account.AccountAccessState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void businessEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/businesses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeeCannotUseAdministrativeEndpoints() throws Exception {
        activeAccount(10L, 1L, false);

        mockMvc.perform(get("/api/businesses/1/sectors")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("10")
                                        .claim("businessId", 1L)
                                        .claim("role", "EMPLOYEE"))
                                .authorities(new SimpleGrantedAuthority(
                                        "ROLE_EMPLOYEE"
                                ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotAccessAnotherBusiness() throws Exception {
        activeAccount(10L, 1L, false);

        mockMvc.perform(get("/api/businesses/2/sectors")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("10")
                                        .claim("businessId", 1L)
                                        .claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void temporaryPasswordOnlyAllowsPasswordChange() throws Exception {
        activeAccount(10L, 1L, true);

        mockMvc.perform(get("/api/businesses/1/sectors")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("10")
                                        .claim("businessId", 1L)
                                        .claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        "Debe cambiar la contraseña antes de continuar"
                ));
    }

    private void activeAccount(
            Long accountId,
            Long businessId,
            boolean mustChangePassword
    ) {
        AccountAccessState state = new AccountAccessState() {
            public boolean getAccountActive() { return true; }
            public boolean getBusinessActive() { return true; }
            public boolean getMustChangePassword() {
                return mustChangePassword;
            }
        };
        when(accountRepository.findAccessState(accountId, businessId))
                .thenReturn(java.util.Optional.of(state));
    }
}
