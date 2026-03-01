package com.inha.borrow.backend.service;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.ResponseType;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.response.PatchResponseDto;
import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.ItemRepository;
import com.inha.borrow.backend.repository.RequestRepository;

@SpringBootTest
@Transactional
class ResponseServiceTest {

        @Autowired
        private ResponseService responseService;

        @Autowired
        private RequestRepository requestRepository;

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private BorrowerRepository borrowerRepository;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        private int itemId;
        private int requestId; // BORROW request
        private int returnRequestId; // RETURN request
        private String borrowerId = "borrower-1";
        private String adminId = "admin-1";
        private String adminDivision = "TEST";

        @BeforeEach
        void setUp() {
                // Clean tables used in this test
                requestRepository.deleteAll();
                itemRepository.deleteAll();
                borrowerRepository.deleteAll();

                // Item
                ItemDto itemDto = new ItemDto("umbrella", "somewhere", "1234", 1000);
                Item savedItem = itemRepository.save(itemDto);
                itemId = savedItem.getId();

                // Ensure admin (FK for request.manager)
                jdbcTemplate.update(
                                "INSERT INTO division(code, name) VALUES(?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name);",
                                adminDivision, "테스트부서");
                jdbcTemplate.update("DELETE FROM admin WHERE id = ?", adminId);
                jdbcTemplate.update(
                                "INSERT INTO admin(id, password, email, name, phonenumber, position, division, , is_delete) VALUES(?, ?, ?, ?, ?, ?, ?, false);",
                                adminId, "$2a$10$abcdefghijklmnopqrstuv", adminId + "@test.com", "관리자", "010-0000-0000",
                                "DIVISION_MEMBER", adminDivision);

                // Borrower (FK for request)
                borrowerRepository.save(BorrowerDto.builder()
                                .id(borrowerId)
                                .name("borrower")
                                .phonenumber("010")
                                .accountNumber("111-11")
                                .build());

                // Request #1: BORROW
                SaveRequestDto saveRequestDto = SaveRequestDto.builder()
                                .itemId(itemId)
                                .borrowerId(borrowerId)
                                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                                .borrowerAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                                .type(RequestType.BORROW)
                                .build();
                requestId = requestRepository.save(saveRequestDto).getRequestId();

                // Prepare for response: assign manager and set state to ASSIGNED
                jdbcTemplate.update("UPDATE request SET manager = ? WHERE id = ?", adminId, requestId);
                requestRepository.updateRequestState(RequestState.ASSIGNED, requestId);

                // Request #2: RETURN (separate request for RETURN-type tests)
                SaveRequestDto saveReturnRequest = SaveRequestDto.builder()
                                .itemId(itemId)
                                .borrowerId(borrowerId)
                                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                                .borrowerAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                                .type(RequestType.RETURN)
                                .build();
                returnRequestId = requestRepository.save(saveReturnRequest).getRequestId();
                jdbcTemplate.update("UPDATE request SET manager = ? WHERE id = ?", adminId, returnRequestId);
                requestRepository.updateRequestState(RequestState.ASSIGNED, returnRequestId);
        }

        @Test
        @DisplayName("createResponse - BORROW 승인: request PERMIT, item BORROWED")
        void createResponse_borrow_permit_success() {
                SaveResponseDto dto = new SaveResponseDto(requestId, null, ResponseType.BORROW);

                Response result = responseService.createResponse(adminId, dto);

                // Response returned
                assertThat(result.getId()).isGreaterThan(0);
                assertThat(result.getRequestId()).isEqualTo(requestId);
                assertThat(result.getType()).isEqualTo(ResponseType.BORROW);

                // Side effects
                RequestState reqState = jdbcTemplate.queryForObject(
                                "SELECT state FROM request WHERE id = ?",
                                (rs, i) -> RequestState.valueOf(rs.getString(1)), requestId);
                ItemState itemState = jdbcTemplate.queryForObject(
                                "SELECT state FROM item WHERE id = ?",
                                (rs, i) -> ItemState.valueOf(rs.getString(1)), itemId);
                assertThat(reqState).isEqualTo(RequestState.PERMIT);
                assertThat(itemState).isEqualTo(ItemState.BORROWED);
        }

        @Test
        @DisplayName("createResponse - BORROW 거절: request REJECT, item AFFORD")
        void createResponse_borrow_reject_success() {
                SaveResponseDto dto = new SaveResponseDto(requestId, "사유", ResponseType.BORROW);

                responseService.createResponse(adminId, dto);

                RequestState reqState = jdbcTemplate.queryForObject(
                                "SELECT state FROM request WHERE id = ?",
                                (rs, i) -> RequestState.valueOf(rs.getString(1)), requestId);
                ItemState itemState = jdbcTemplate.queryForObject(
                                "SELECT state FROM item WHERE id = ?",
                                (rs, i) -> ItemState.valueOf(rs.getString(1)), itemId);
                assertThat(reqState).isEqualTo(RequestState.REJECT);
                assertThat(itemState).isEqualTo(ItemState.AFFORD);
        }

        @Test
        @DisplayName("createResponse - RETURN 승인: request PERMIT, item AFFORD (separate request)")
        void createResponse_return_permit_success() {
                SaveResponseDto dto = new SaveResponseDto(returnRequestId, null, ResponseType.RETURN);
                responseService.createResponse(adminId, dto);

                RequestState reqState = jdbcTemplate.queryForObject(
                                "SELECT state FROM request WHERE id = ?",
                                (rs, i) -> RequestState.valueOf(rs.getString(1)), returnRequestId);
                ItemState itemState = jdbcTemplate.queryForObject(
                                "SELECT state FROM item WHERE id = ?",
                                (rs, i) -> ItemState.valueOf(rs.getString(1)), itemId);
                assertThat(reqState).isEqualTo(RequestState.PERMIT);
                assertThat(itemState).isEqualTo(ItemState.AFFORD);
        }

        @Test
        @DisplayName("createResponse - RETURN 거절: request REJECT, item REVIEWING (separate request)")
        void createResponse_return_reject_success() {
                SaveResponseDto dto = new SaveResponseDto(returnRequestId, "사유", ResponseType.RETURN);
                responseService.createResponse(adminId, dto);

                RequestState reqState = jdbcTemplate.queryForObject(
                                "SELECT state FROM request WHERE id = ?",
                                (rs, i) -> RequestState.valueOf(rs.getString(1)), returnRequestId);
                ItemState itemState = jdbcTemplate.queryForObject(
                                "SELECT state FROM item WHERE id = ?",
                                (rs, i) -> ItemState.valueOf(rs.getString(1)), itemId);
                assertThat(reqState).isEqualTo(RequestState.REJECT);
                assertThat(itemState).isEqualTo(ItemState.REVIEWING);
        }

        @Test
        @DisplayName("createResponse - 담당자 불일치 AccessDeniedException")
        void createResponse_manager_mismatch() {
                SaveResponseDto dto = new SaveResponseDto(requestId, null, ResponseType.BORROW);
                assertThatThrownBy(() -> responseService.createResponse("other-admin", dto))
                                .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("createResponse - 요청/응답 타입 불일치 InvalidValueException")
        void createResponse_type_mismatch() {
                SaveResponseDto dto = new SaveResponseDto(requestId, null, ResponseType.RETURN);
                assertThatThrownBy(() -> responseService.createResponse(adminId, dto))
                                .isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("createResponse - 요청 상태 ASSIGNED 아님 InvalidValueException")
        void createResponse_state_not_assigned() {
                // set another state
                requestRepository.updateRequestState(RequestState.PERMIT, requestId);
                SaveResponseDto dto = new SaveResponseDto(requestId, null, ResponseType.BORROW);
                assertThatThrownBy(() -> responseService.createResponse(adminId, dto))
                                .isInstanceOf(InvalidValueException.class);
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("updateResponse - RETURN 거절에서 승인 전환: request PERMIT, item AFFORD, response 갱신")
        void updateResponse_return_reject_to_permit_success() {
                // Prepare RETURN request in REJECT state with same manager
                jdbcTemplate.update("UPDATE request SET state = ? WHERE id = ?",
                                RequestState.REJECT.name(), returnRequestId);

                // Insert a response row to be updated
                KeyHolder kh = new GeneratedKeyHolder();
                jdbcTemplate.update(con -> {
                        PreparedStatement ps = con.prepareStatement(
                                        "INSERT INTO response(request_id, reject_reason, type) VALUES(?, ?, ?)",
                                        Statement.RETURN_GENERATED_KEYS);
                        ps.setInt(1, returnRequestId);
                        ps.setString(2, "이전사유");
                        ps.setString(3, ResponseType.RETURN.name());
                        return ps;
                }, kh);
                int responseId = kh.getKey().intValue();

                // Update: empty reason means permit
                PatchResponseDto patch = PatchResponseDto.builder()
                                .requestId(returnRequestId)
                                .rejectReason("")
                                .build();
                responseService.updateResponse(adminId, String.valueOf(responseId), patch);

                RequestState reqState = jdbcTemplate.queryForObject(
                                "SELECT state FROM request WHERE id = ?",
                                (rs, i) -> RequestState.valueOf(rs.getString(1)), returnRequestId);
                ItemState itemState = jdbcTemplate.queryForObject(
                                "SELECT state FROM item WHERE id = ?",
                                (rs, i) -> ItemState.valueOf(rs.getString(1)), itemId);
                String rejectReason = jdbcTemplate.queryForObject(
                                "SELECT reject_reason FROM response WHERE id = ?",
                                String.class, responseId);

                assertThat(reqState).isEqualTo(RequestState.PERMIT);
                assertThat(itemState).isEqualTo(ItemState.AFFORD);
                assertThat(rejectReason).isEmpty();
        }

        @Test
        @DisplayName("updateResponse - BORROW 타입은 허용 안됨")
        void updateResponse_not_allowed_for_borrow() {
                // Ensure request is BORROW and REJECT to pass later state check
                jdbcTemplate.update("UPDATE request SET type = ?, state = ?, manager = ? WHERE id = ?",
                                RequestType.BORROW.name(), RequestState.REJECT.name(), adminId, requestId);

                PatchResponseDto patch = PatchResponseDto.builder()
                                .requestId(requestId)
                                .rejectReason("")
                                .build();

                assertThatThrownBy(() -> responseService.updateResponse(adminId, "1", patch))
                                .isInstanceOf(com.inha.borrow.backend.model.exception.InvalidValueException.class);
        }
}
