package com.gydl.djbh.security;

import com.gydl.djbh.entity.TUser;
import com.gydl.djbh.mapper.TUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证服务
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被禁用");
        }

        // 加载用户权限列表
        List<String> permissions = userMapper.findPermissionsByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        // 也添加角色权限
        if (user.getRoleCode() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleCode()));
        }

        boolean locked = user.getLockedUntil() != null &&
                user.getLockedUntil().isAfter(java.time.LocalDateTime.now());

        return new JwtUserDetails(
                user.getId(),
                username,
                user.getPasswordHash(),
                user.getRoleCode(),
                authorities
        );
    }
}
