package com.lsq.coreplatform.dto.request;

import com.lsq.coreplatform.generated.enums.BooksAvailabilitystatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookDTO {
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String author;

    @Pattern(regexp = "^(\\d{10}|\\d{13})$")
    private String isbn;

    private Integer publishedYear;

    @NotNull
    private BooksAvailabilitystatus availabilityStatus;
}
