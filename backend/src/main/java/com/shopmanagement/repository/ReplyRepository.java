package com.shopmanagement.repository;

import com.shopmanagement.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    /**
     * Find all replies for a specific post
     */
    @Query("SELECT r FROM Reply r WHERE r.post.id = :postId ORDER BY r.createdAt ASC")
    List<Reply> findByPostId(@Param("postId") Long postId);
    
    /**
     * Find all replies for a specific post with pagination
     */
    @Query("SELECT r FROM Reply r WHERE r.post.id = :postId")
    Page<Reply> findByPostId(@Param("postId") Long postId, Pageable pageable);
    
    /**
     * Find reply by ID
     */
    Optional<Reply> findById(Long id);
    
    /**
     * Find replies by author ID
     */
    @Query("SELECT r FROM Reply r WHERE r.author.id = :authorId")
    Page<Reply> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    /**
     * Count replies for a specific post
     */
    @Query("SELECT COUNT(r) FROM Reply r WHERE r.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);
}
