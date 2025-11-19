package com.shopmanagement.controller;

import com.shopmanagement.dto.request.PostRequest;
import com.shopmanagement.dto.request.ReplyRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.PostResponse;
import com.shopmanagement.dto.response.ReplyResponse;
import com.shopmanagement.entity.Post;
import com.shopmanagement.entity.Reply;
import com.shopmanagement.service.CommunityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "Community", description = "Endpoints for community posts, replies, and discussions")
@SecurityRequirement(name = "bearerAuth")
public class CommunityController {
    
    private final CommunityService communityService;
    
    /**
     * Get all posts with pagination and optional search/filter
     */
    @GetMapping("/posts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long shopId) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Post> postPage;
        
        // Handle different filter scenarios
        if (search != null && !search.trim().isEmpty()) {
            postPage = communityService.searchPosts(search, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            postPage = communityService.getPostsByCategory(category, pageable);
        } else if (shopId != null) {
            postPage = communityService.getPostsByShop(shopId, pageable);
        } else {
            postPage = communityService.getAllPosts(pageable);
        }
        
        List<PostResponse> postResponses = postPage.getContent().stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", postResponses);
        response.put("page", postPage.getNumber());
        response.put("size", postPage.getSize());
        response.put("totalElements", postPage.getTotalElements());
        response.put("totalPages", postPage.getTotalPages());
        response.put("last", postPage.isLast());
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(response)
                .message("Posts retrieved successfully")
                .build());
    }
    
    /**
     * Get post by ID with replies
     */
    @GetMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        Post post = communityService.getPostById(id);
        List<Reply> replies = communityService.getRepliesByPostId(id);
        
        PostResponse response = mapToPostResponseWithReplies(post, replies);
        
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .success(true)
                .data(response)
                .message("Post retrieved successfully")
                .build());
    }
    
    /**
     * Create a new post
     */
    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request) {
        
        Post post = mapToPostEntity(request);
        Post createdPost = communityService.createPost(post);
        PostResponse response = mapToPostResponse(createdPost);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PostResponse>builder()
                        .success(true)
                        .data(response)
                        .message("Post created successfully")
                        .build());
    }
    
    /**
     * Update an existing post
     */
    @PutMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        
        Post postUpdate = mapToPostEntity(request);
        Post updatedPost = communityService.updatePost(id, postUpdate);
        PostResponse response = mapToPostResponse(updatedPost);
        
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .success(true)
                .data(response)
                .message("Post updated successfully")
                .build());
    }
    
    /**
     * Delete a post
     */
    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Post deleted successfully")
                .build());
    }
    
    /**
     * Get all replies for a post
     */
    @GetMapping("/posts/{id}/replies")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ReplyResponse>>> getReplies(@PathVariable Long id) {
        List<Reply> replies = communityService.getRepliesByPostId(id);
        
        List<ReplyResponse> replyResponses = replies.stream()
                .map(this::mapToReplyResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.<List<ReplyResponse>>builder()
                .success(true)
                .data(replyResponses)
                .message("Replies retrieved successfully")
                .build());
    }
    
    /**
     * Add a reply to a post
     */
    @PostMapping("/posts/{id}/replies")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            @PathVariable Long id,
            @Valid @RequestBody ReplyRequest request) {
        
        Reply reply = mapToReplyEntity(request);
        Reply createdReply = communityService.createReply(id, reply);
        ReplyResponse response = mapToReplyResponse(createdReply);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReplyResponse>builder()
                        .success(true)
                        .data(response)
                        .message("Reply added successfully")
                        .build());
    }
    
    /**
     * Delete a reply
     */
    @DeleteMapping("/replies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteReply(@PathVariable Long id) {
        communityService.deleteReply(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Reply deleted successfully")
                .build());
    }
    
    /**
     * Map Post entity to PostResponse DTO (without replies)
     */
    private PostResponse mapToPostResponse(Post post) {
        Long replyCount = communityService.getReplyCount(post.getId());
        
        return PostResponse.builder()
                .id(post.getId())
                .author(PostResponse.AuthorSummary.builder()
                        .id(post.getAuthor().getId())
                        .fullName(post.getAuthor().getFullName())
                        .email(post.getAuthor().getEmail())
                        .shopId(post.getAuthor().getShop().getId())
                        .build())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .replyCount(replyCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
    
    /**
     * Map Post entity to PostResponse DTO (with replies)
     */
    private PostResponse mapToPostResponseWithReplies(Post post, List<Reply> replies) {
        List<ReplyResponse> replyResponses = replies.stream()
                .map(this::mapToReplyResponse)
                .collect(Collectors.toList());
        
        return PostResponse.builder()
                .id(post.getId())
                .author(PostResponse.AuthorSummary.builder()
                        .id(post.getAuthor().getId())
                        .fullName(post.getAuthor().getFullName())
                        .email(post.getAuthor().getEmail())
                        .shopId(post.getAuthor().getShop().getId())
                        .build())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .replyCount((long) replies.size())
                .replies(replyResponses)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
    
    /**
     * Map PostRequest DTO to Post entity
     */
    private Post mapToPostEntity(PostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        return post;
    }
    
    /**
     * Map Reply entity to ReplyResponse DTO
     */
    private ReplyResponse mapToReplyResponse(Reply reply) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .postId(reply.getPost().getId())
                .author(ReplyResponse.AuthorSummary.builder()
                        .id(reply.getAuthor().getId())
                        .fullName(reply.getAuthor().getFullName())
                        .email(reply.getAuthor().getEmail())
                        .shopId(reply.getAuthor().getShop().getId())
                        .build())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }
    
    /**
     * Map ReplyRequest DTO to Reply entity
     */
    private Reply mapToReplyEntity(ReplyRequest request) {
        Reply reply = new Reply();
        reply.setContent(request.getContent());
        return reply;
    }
}
