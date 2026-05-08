package com.example.grade.security;

import com.example.grade.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.time.LocalDateTime;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long userId;
    private final String realName;
    private final LocalDateTime createdAt;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        super(
            user.getUsername(),
            user.getPasswordHash(),
            user.getStatus() == null || user.getStatus() == 1,
            true,
            true,
            true,
            authorities
        );
        this.userId = user.getId();
        this.realName = user.getRealName();
        this.createdAt = user.getCreatedAt();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return super.getPassword();
    }
}
