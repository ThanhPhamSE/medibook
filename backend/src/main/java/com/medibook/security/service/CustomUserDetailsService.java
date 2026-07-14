package com.medibook.security.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.security.context.CurrentUserRequestCache;
import com.medibook.security.model.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CurrentUserRequestCache currentUserRequestCache;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findWithRoleByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        currentUserRequestCache.setUser(user);

        String roleName = user.getRole() != null ? user.getRole().getName() : "CUSTOMER";

        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                roleName,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }
}
