package com.shopmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    
    private Long id;
    private AuthorSummary author;
    private String title;
    private String content;
    private String category;
    private Long replyCount;
    private List<ReplyResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSummary {
        private Long id;
        private String fullName;
        private String email;
        private Long shopId;
    }
}
