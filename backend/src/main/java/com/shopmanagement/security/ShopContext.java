package com.shopmanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ShopContext {
    
    /**
     * Get the current authenticated user's shop ID from the security context
     * @return shopId of the authenticated user
     * @throws RuntimeException if user is not authenticated
     */
    public static Long getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getShopId();
        }
        
        throw new RuntimeException("Unable to extract shop ID from security context");
    }
    
    /**
     * Get the current authenticated user's ID from the security context
     * @return userId of the authenticated user
     * @throws RuntimeException if user is not authenticated
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        
        throw new RuntimeException("Unable to extract user ID from security context");
    }
    
    /**
     * Get the current authenticated user's role from the security context
     * @return role of the authenticated user
     * @throws RuntimeException if user is not authenticated
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getRole();
        }
        
        throw new RuntimeException("Unable to extract user role from security context");
    }
    
    /**
     * Get the current authenticated UserPrincipal
     * @return UserPrincipal of the authenticated user
     * @throws RuntimeException if user is not authenticated
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        
        throw new RuntimeException("Unable to extract user from security context");
    }
    
    /**
     * Check if the current user has ADMIN role
     * @return true if user is ADMIN, false otherwise
     */
    public static boolean isAdmin() {
        try {
            String role = getCurrentUserRole();
            return "ADMIN".equals(role);
        } catch (RuntimeException e) {
            return false;
        }
    }
    
    /**
     * Check if the current user has STAFF role
     * @return true if user is STAFF, false otherwise
     */
    public static boolean isStaff() {
        try {
            String role = getCurrentUserRole();
            return "STAFF".equals(role);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
