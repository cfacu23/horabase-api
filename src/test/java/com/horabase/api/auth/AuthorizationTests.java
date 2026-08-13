package com.horabase.api.auth;

import com.horabase.api.account.AccountRepository;
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
        when(accountRepository
                .existsByIdAndActiveTrueAndBusiness_IdAndBusiness_ActiveTrue(
                        10L, 1L
                ))
                .thenReturn(true);

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
        when(accountRepository
                .existsByIdAndActiveTrueAndBusiness_IdAndBusiness_ActiveTrue(
                        10L, 1L
                ))
                .thenReturn(true);

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
}
