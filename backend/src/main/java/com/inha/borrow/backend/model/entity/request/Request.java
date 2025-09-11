package com.inha.borrow.backend.model.entity.request;

import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.entity.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Builder
public class Request {
    private int id;
    private int itemId;
    private RequestManager manager;
    private String borrowerId;
    private Timestamp createdAt;
    private Timestamp returnAt;
    private Timestamp borrowAt;
    private RequestType type;
    private RequestState state;
    private Boolean cancel;
    private Response response;
}
