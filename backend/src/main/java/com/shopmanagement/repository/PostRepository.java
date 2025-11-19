package com.shopmanagement.repository;

import com.shopmanagement.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * Find all posts with pagination and sorting
     */
    Page<Post> findAll(Pageable pageable);
    
    /**
     * Find all posts by shop ID with pagination
     */
    Page<Post> findByShopId(Long shopId, Pageable pageable);
    
    /**
     * Find post by ID and shop ID (for multi-tenant isolation)
     */
    Optional<Post> findByIdAndShopId(Long id, Long shopId);
    
    /**
     * Find posts by category with pagination
     */
    Page<Post> findByCategory(String category, Pageable pageable);
    
    /**
     * Find posts by category and shop ID with pagination
     */
    Page<Post> findByShopIdAndCategory(Long shopId, String category, Pageable pageable);
    
    /**
     * Search posts by title or content
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Post> searchPosts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search posts by title or content within a shop
     */
    @Query("SELECT p FROM Post p WHERE p.shopId = :shopId AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Post> searchPostsByShop(@Param("shopId") Long shopId, 
                                  @Param("searchTerm") String searchTerm, 
                                  Pageable pageable);
    
    /**
     * Find posts by author ID
     */
    @Query("SELECT p FROM Post p WHERE p.author.id = :authorId")
    Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}
