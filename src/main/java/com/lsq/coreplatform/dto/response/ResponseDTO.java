package com.lsq.coreplatform.dto.response;

import lombok.Data;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
public class ResponseDTO {
    private Long id;
    private String message;
}
