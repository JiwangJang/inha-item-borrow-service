package com.inha.borrow.backend.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminAuthenticationFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginSuccess() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_admin",
                        "password": "1234"
                    }
                """;

        mockMvc.perform(post("/admins/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk());
    }

    @Test
    void loginFailureWithIncorrectId() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_admi",
                        "password": "123"
                    }
                """;

        mockMvc.perform(post("/admins/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginFailureWithIncorrectPassword() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_admin",
                        "password": "123"
                    }
                """;

        mockMvc.perform(post("/admins/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

}
