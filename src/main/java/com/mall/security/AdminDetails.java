package com.mall.security;

import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsResource;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AdminDetails implements UserDetails {

    private final UmsAdmin admin;
    private final List<UmsResource> resources;

    public AdminDetails(
            UmsAdmin admin,
            List<UmsResource> resources) {

        this.admin = admin;
        this.resources = List.copyOf(resources);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities =
                new java.util.ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        resources.stream()
                .map(UmsResource::getCode)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return authorities;
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return Integer.valueOf(1)
                .equals(admin.getStatus());
    }
}