package com.project.Transflow.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "문서 락 상태 응답")
public class LockStatusResponse {

    @Schema(description = "락 여부", example = "true")
    private Boolean locked;

    @Schema(description = "락한 사용자 정보")
    private LockedByInfo lockedBy;

    @Schema(description = "락 시간", example = "2024-01-01T00:00:00")
    private LocalDateTime lockedAt;

    @Schema(description = "편집 가능 여부", example = "true")
    private Boolean canEdit;

    @Schema(description = "완료된 문단 인덱스 목록", example = "[0, 1, 2]")
    private List<Integer> completedParagraphs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "락한 사용자 정보")
    public static class LockedByInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Schema(description = "사용자 이메일", example = "user@example.com")
        private String email;
    }
}

