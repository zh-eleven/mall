package com.mall.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.mapper.UmsAdminMapper;
import com.mall.admin.mapper.UmsAdminRoleRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDetailsService
        implements UserDetailsService {

    private final UmsAdminMapper adminMapper;

    private final UmsAdminRoleRelationMapper
            adminRoleRelationMapper;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UmsAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<UmsAdmin>()
                        .eq(
                                UmsAdmin::getUsername,
                                username
                        )
        );

        if (admin == null) {
            throw new UsernameNotFoundException(
                    "用户名或密码错误"
            );
        }

        List<UmsResource> resources =
                adminRoleRelationMapper
                        .selectResourceListByAdminId(
                                admin.getId()
                        );

        return new AdminDetails(admin, resources);
    }
}