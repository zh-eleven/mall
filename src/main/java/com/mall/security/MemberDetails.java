package com.mall.security;

import com.mall.domain.entity.UmsMember;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class MemberDetails implements UserDetails {

    private final UmsMember member;

    public MemberDetails(UmsMember member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 用户权限后面 RBAC 再做
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return member.getStatus() == 1;
    }
}