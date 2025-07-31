package com.inha.borrow.backend.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.cache.SignUpRequestSessionCache;
import com.inha.borrow.backend.model.auth.SignUpRequestSession;

@SpringBootTest
@AutoConfigureMockMvc
public class BorrowerVerificationControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SignUpRequestSessionCache signUpRequestSessionCache;

    @Autowired
    public BorrowerVerificationControllerTest(MockMvc mockMvc, ObjectMapper objectMapper,
            SignUpRequestSessionCache signUpRequestSessionCache) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.signUpRequestSessionCache = signUpRequestSessionCache;
    }

    @Test
    void verifyIdSuccess() throws Exception {
        // given
        String id = "jiwang917";
        // when
        // then
        mockMvc.perform(get("/borrowers/auth/id-check?id=" + id))
                .andExpect(status().isOk());
        
        Opn SignUpRequestSession session = signUpRequestSessionCache.get(id);
    }
}
