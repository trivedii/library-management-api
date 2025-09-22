package com.lsq.coreplatform.dto.response;

import com.lsq.coreplatform.dto.request.BookDTO;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class BookSearchResponseDTO {
    private Integer totalCount;
    private Set<BookDTO> books;
}
