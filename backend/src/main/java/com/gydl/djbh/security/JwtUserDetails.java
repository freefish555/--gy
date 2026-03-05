package com.gydl.djbh.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 携带userId的自定义UserDetails
 */
@Getter
public class JwtUserDetails extends User {

    private final Long userId;
    private final String roleCode;

    public JwtUserDetails(Long userId, String username, String password,
                          String roleCode,
                          Collection<? extends GrantedAuthority> authorities) {
        super(username, password, true, true, true, false, authorities);
        this.userId = userId;
        this.roleCode = roleCode;
    }
}
