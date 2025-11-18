package com.shopmanagement.service;

import com.shopmanagement.dto.request.LoginRequest;
import com.shopmanagement.dto.request.RefreshTokenRequest;
import com.shopmanagement.dto.request.RegisterRequest;
import com.shopmanagement.dto.response.AuthResponse;
import com.shopmanagement.entity.*;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.repository.WalletRepository;
import com.shopmanagement.security.JwtTokenProvider;
import com.shopmanagement.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final WalletService walletService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate unique constraints
        if (shopRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }
        
        if (shopRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        // Create shop
        Shop shop = new Shop();
        shop.setShopName(request.getShopName());
        shop.setShopType(ShopType.valueOf(request.getShopType().toUpperCase()));
        shop.setGstNumber(request.getGstNumber());
        shop.setOwnerName(request.getOwnerName());
        shop.setUsername(request.getUsername());
        shop.setPhoneNumber(request.getPhoneNumber());
        shop.setCountryCode(request.getCountryCode());
        shop.setAddress(request.getAddress());
        shop.setEmail(request.getEmail());
        shop.setLogoUrl(request.getLogoUrl());
        shop.setActive(true);
        
        Shop savedShop = shopRepository.save(shop);
        
        // Create admin user for the shop
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getOwnerName());
        user.setRole(UserRole.ADMIN);
        user.setShop(savedShop);
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Create wallet with unique referral code
        Wallet wallet = new Wallet();
        wallet.setShop(savedShop);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setReferralCode(generateUniqueReferralCode());
        
        walletRepository.save(wallet);
        
        // Apply referral code if provided
        if (request.getReferralCode() != null && !request.getReferralCode().trim().isEmpty()) {
            try {
                walletService.applyReferralCode(request.getReferralCode(), savedShop.getId());
                // Credit referral bonus immediately upon successful registration
                walletService.creditReferralBonus(savedShop.getId());
            } catch (Exception e) {
                log.warn("Failed to apply referral code during registration: {}", e.getMessage());
                // Don't fail registration if referral code is invalid
            }
        }
        
        // Authenticate and generate tokens
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .shopId(savedShop.getId())
                .shopName(savedShop.getShopName())
                .role(savedUser.getRole().name())
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Shop shop = shopRepository.findById(userPrincipal.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .shopId(userPrincipal.getShopId())
                .shopName(shop.getShopName())
                .role(userPrincipal.getRole())
                .build();
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        
        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .shopId(user.getShop().getId())
                .shopName(user.getShop().getShopName())
                .role(user.getRole().name())
                .build();
    }
    
    private String generateUniqueReferralCode() {
        String referralCode;
        do {
            referralCode = "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (walletRepository.existsByReferralCode(referralCode));
        
        return referralCode;
    }
}
