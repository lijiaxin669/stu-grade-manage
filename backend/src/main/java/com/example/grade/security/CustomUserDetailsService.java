package com.example.grade.security;

import com.example.grade.domain.User;
import com.example.grade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .flatMap(role -> {
                java.util.stream.Stream<SimpleGrantedAuthority> roleAuth = java.util.stream.Stream.of(new SimpleGrantedAuthority(role.getCode()));
                // Backward-compatible alias: ROLE_ADMIN is merged into ROLE_SUPER_ADMIN
                if ("ROLE_ADMIN".equals(role.getCode())) {
                    roleAuth = java.util.stream.Stream.concat(roleAuth, java.util.stream.Stream.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
                }
                java.util.stream.Stream<SimpleGrantedAuthority> permAuth = role.getPermissions().stream()
                    .map(p -> new SimpleGrantedAuthority(p.getCode()));
                return java.util.stream.Stream.concat(roleAuth, permAuth);
            })
            .distinct()
            .collect(Collectors.toList());

        return new CustomUserDetails(user, authorities);
    }
}
