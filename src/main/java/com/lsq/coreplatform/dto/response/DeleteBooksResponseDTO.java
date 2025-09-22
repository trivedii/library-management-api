package com.lsq.coreplatform.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DeleteBooksResponseDTO {
    private List<Long> deletedBookIds;       // Successfully deleted
    private List<Long> notDeletedBookIds;    // Could not delete
    private Map<Long, String> reasons;       // Why not deleted (borrowed, not found, etc.)

    public DeleteBooksResponseDTO() {
        deletedBookIds = new ArrayList<>();
        notDeletedBookIds = new ArrayList<>();
        reasons = new HashMap<>();
    }
}
