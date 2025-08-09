package com.inha.borrow.backend.authentication.intergration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.model.response.ApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class BorrowerAuthenticationFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        MvcResult result = mockMvc.perform(post("/borrowers/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(content, ApiResponse.class);

        assertEquals(response.isSuccess(), false);
        assertEquals(response.getData(), "CHECK_YOUR_INFO");
    }

    @Test
    void loginFailureWithIncorrectPassword() throws Exception {
        String loginRequest = """
                    {
                        "id": "test_borrower",
                        "password": "123"
                    }
                """;

        MvcResult result = mockMvc.perform(post("/borrowers/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse<String> response = objectMapper.readValue(content, ApiResponse.class);

        assertEquals(response.isSuccess(), false);
        assertEquals(response.getData(), "CHECK_YOUR_INFO");
    }

}
