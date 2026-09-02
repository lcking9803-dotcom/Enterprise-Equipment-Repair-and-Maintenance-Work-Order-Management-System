package com.lwq.maintenance.web;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lwq.maintenance.common.ApiResponse;
import com.lwq.maintenance.domain.entity.SysUser;
import com.lwq.maintenance.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final SysUserMapper userMapper;

    @GetMapping("/maintainers")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ApiResponse<List<SysUser>> maintainers() {
        List<SysUser> users = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRoleCode, "MAINTAINER").eq(SysUser::getEnabled, true)
                .select(SysUser::getId, SysUser::getUsername, SysUser::getDisplayName,
                        SysUser::getRoleCode, SysUser::getDepartment));
        return ApiResponse.ok(users);
    }
}
