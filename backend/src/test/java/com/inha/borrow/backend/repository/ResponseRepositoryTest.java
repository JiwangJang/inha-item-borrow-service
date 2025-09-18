package com.inha.borrow.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.ResponseType;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@JdbcTest
@Import({ ResponseRepository.class, RequestRepository.class, ItemRepository.class, BorrowerRepository.class })
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
class ResponseRepositoryTest {

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int requestId;

    @BeforeEach
    void setUp() {
        // clean related tables
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        borrowerRepository.deleteAll();

        // prepare item
        ItemDto itemDto = new ItemDto();
        itemDto.setName("item-1");
        itemDto.setPassword("pw");
        itemDto.setLocation("loc");
        itemDto.setPrice(1000);
        Item item = itemRepository.save(itemDto);

        // prepare borrower
        BorrowerDto borrower = BorrowerDto.builder()
                .id("borrower-1")
                .password("pw")
                .email("a@b.c")
                .name("name")
                .phonenumber("010-0000-0000")
                .studentNumber("20250000")
                .accountNumber("123-456")
                .refreshToken("rtk")
                .build();
        borrowerRepository.save(borrower);

        // prepare request referencing item + borrower
        SaveRequestDto saveRequestDto = SaveRequestDto.builder()
                .itemId(item.getId())
                .borrowerId(borrower.getId())
                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                .borrowerAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                .type(RequestType.BORROW)
                .build();
        requestId = requestRepository.save(saveRequestDto).getRequestId();
    }

    @Test
    @DisplayName("응답 저장 성공 (FK: request -> item)")
    void save_success() {
        SaveResponseDto dto = new SaveResponseDto(requestId, "", ResponseType.BORROW);

        Response saved = responseRepository.save(dto);

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getRequestId()).isEqualTo(requestId);
        assertThat(saved.getType()).isEqualTo(ResponseType.BORROW);
        assertThat(saved.getRejectReason()).isEqualTo("");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("응답 수정 성공 (reject_reason 변경)")
    void update_success() {
        // Insert response directly to avoid relying on save() return id
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO response(request_id, reject_reason, type) VALUES(?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, requestId);
            ps.setString(2, "old-reason");
            ps.setString(3, ResponseType.BORROW.name());
            return ps;
        }, kh);
        int responseId = kh.getKey().intValue();

        responseRepository.update(String.valueOf(responseId), "new-reason");

        String updated = jdbcTemplate.queryForObject(
                "SELECT reject_reason FROM response WHERE id = ?",
                String.class,
                responseId);
        assertThat(updated).isEqualTo("new-reason");
    }

    @Test
    @DisplayName("응답 수정 실패 (존재하지 않는 ID)")
    void update_fail_not_found() {
        assertThatThrownBy(() -> responseRepository.update("999999", "reason"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
