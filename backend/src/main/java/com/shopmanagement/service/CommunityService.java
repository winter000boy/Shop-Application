package com.shopmanagement.service;

import com.shopmanagement.entity.Post;
import com.shopmanagement.entity.Reply;
import com.shopmanagement.entity.User;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.UnauthorizedException;
import com.shopmanagement.repository.PostRepository;
import com.shopmanagement.repository.ReplyRepository;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {
    
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all posts with pagination and sorting
     * Posts are visible across all shops (community feature)
     */
    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }
    
    /**
     * Get posts by shop ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<Post> getPostsByShop(Long shopId, Pageable pageable) {
        return postRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Get posts for the current shop
     */
    @Transactional(readOnly = true)
    public Page<Post> getCurrentShopPosts(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return postRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Get posts by category with pagination
     */
    @Transactional(readOnly = true)
    public Page<Post> getPostsByCategory(String category, Pageable pageable) {
        return postRepository.findByCategory(category, pageable);
    }
    
    /**
     * Search posts by title or content
     */
    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return postRepository.findAll(pageable);
        }
        return postRepository.searchPosts(searchTerm.trim(), pageable);
    }
    
    /**
     * Get post by ID
     */
    @Transactional(readOnly = true)
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
    }
    
    /**
     * Create a new post
     */
    @Transactional
    public Post createPost(Post post) {
        Long shopId = ShopContext.getCurrentShopId();
        Long userId = ShopContext.getCurrentUserId();
        
        // Get the current user
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        
        // Set post properties
        post.setShopId(shopId);
        post.setAuthor(author);
        
        return postRepository.save(post);
    }
    
    /**
     * Update an existing post
     * Only the author can update their post
     */
    @Transactional
    public Post updatePost(Long postId, Post postUpdate) {
        Long userId = ShopContext.getCurrentUserId();
        
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
        
        // Check if the current user is the author
        if (!existingPost.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You are not authorized to update this post");
        }
        
        // Update fields
        existingPost.setTitle(postUpdate.getTitle());
        existingPost.setContent(postUpdate.getContent());
        if (postUpdate.getCategory() != null) {
            existingPost.setCategory(postUpdate.getCategory());
        }
        
        return postRepository.save(existingPost);
    }
    
    /**
     * Delete a post
     * Only the author can delete their post
     */
    @Transactional
    public void deletePost(Long postId) {
        Long userId = ShopContext.getCurrentUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
        
        // Check if the current user is the author
        if (!post.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You are not authorized to delete this post");
        }
        
        postRepository.delete(post);
    }
    
    /**
     * Get all replies for a post
     */
    @Transactional(readOnly = true)
    public List<Reply> getRepliesByPostId(Long postId) {
        // Verify post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
        
        return replyRepository.findByPostId(postId);
    }
    
    /**
     * Get all replies for a post with pagination
     */
    @Transactional(readOnly = true)
    public Page<Reply> getRepliesByPostId(Long postId, Pageable pageable) {
        // Verify post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
        
        return replyRepository.findByPostId(postId, pageable);
    }
    
    /**
     * Create a reply to a post
     */
    @Transactional
    public Reply createReply(Long postId, Reply reply) {
        Long userId = ShopContext.getCurrentUserId();
        
        // Get the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
        
        // Get the current user
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        
        // Set reply properties
        reply.setPost(post);
        reply.setAuthor(author);
        
        return replyRepository.save(reply);
    }
    
    /**
     * Delete a reply
     * Only the author can delete their reply
     */
    @Transactional
    public void deleteReply(Long replyId) {
        Long userId = ShopContext.getCurrentUserId();
        
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reply not found with id: " + replyId));
        
        // Check if the current user is the author
        if (!reply.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You are not authorized to delete this reply");
        }
        
        replyRepository.delete(reply);
    }
    
    /**
     * Get reply count for a post
     */
    @Transactional(readOnly = true)
    public Long getReplyCount(Long postId) {
        return replyRepository.countByPostId(postId);
    }
}
