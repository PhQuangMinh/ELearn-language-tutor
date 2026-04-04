package com.example.BTL_Mobile.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminImportResult {
    @Builder.Default
    private int successCount = 0;

    @Builder.Default
    private int errorCount = 0;

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
