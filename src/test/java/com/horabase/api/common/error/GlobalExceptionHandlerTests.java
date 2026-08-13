package com.horabase.api.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsFieldErrorsForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "taxId": "",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Error de validación"))
                .andExpect(jsonPath("$.path")
                        .value("/api/businesses"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.taxId").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void returnsCleanResponseForBusinessError() throws Exception {
        mockMvc.perform(get("/api/businesses/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("No se encontró el comercio"))
                .andExpect(jsonPath("$.path")
                        .value("/api/businesses/999999"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
