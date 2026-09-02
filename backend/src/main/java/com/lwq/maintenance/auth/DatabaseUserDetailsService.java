package com.lwq.maintenance.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lwq.maintenance.domain.entity.SysUser;
import com.lwq.maintenance.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (user == null) throw new UsernameNotFoundException("用户不存在");
        return UserPrincipal.from(user);
    }
}

