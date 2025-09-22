package com.lsq.coreplatform.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class ErrorResponseDTO extends ResponseDTO {
    private String errorCode;
    private Map<String, Object> details;
}
