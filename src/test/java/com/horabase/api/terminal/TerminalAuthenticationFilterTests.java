package com.horabase.api.terminal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TerminalAuthenticationFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void checkInRejectsMissingDeviceCredentials() throws Exception {
        mockMvc.perform(post(
                                "/api/terminal/businesses/{businessId}/check-in",
                                1
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": 1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Credenciales de terminal inválidas"));
    }
}
