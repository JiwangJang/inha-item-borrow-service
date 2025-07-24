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
public class BorrowerAuthenticationFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginSuccess() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_borrower",
                        "password": "1234"
                    }
                """;

        mockMvc.perform(post("/borrowers/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk());
    }

    @Test
    void loginFailureWithIncorrectId() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_admin",
                        "password": "123"
                    }
                """;

        mockMvc.perform(post("/borrowers/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginFailureWithIncorrectPassword() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_borrower",
                        "password": "123"
                    }
                """;

        mockMvc.perform(post("/borrowers/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

}
