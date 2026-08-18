package com.mall.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.domain.entity.UmsMember;
import com.mall.mapper.UmsMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final UmsMemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {

        UmsMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<UmsMember>()
                        .eq(UmsMember::getUsername, username)
        );

        if (member == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        return new MemberDetails(member);
    }
}