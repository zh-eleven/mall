package com.mall.member.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.dto.MemberLoginDTO;
import com.mall.member.entity.UmsMember;
import com.mall.member.mapper.UmsMemberMapper;
import com.mall.member.service.impl.UmsMemberServiceImpl;
import com.mall.security.JwtTokenService;
import com.mall.security.MemberDetails;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAuthenticationStatusTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(UmsMember.class);
    }

    @Mock
    private UmsMemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Test
    void disabledMemberShouldNotLoginOrAuthenticateWithExistingToken() {
        UmsMember disabled = new UmsMember();
        disabled.setId(7L);
        disabled.setUsername("disabled-user");
        disabled.setPassword("hash");
        disabled.setStatus(0);

        when(memberMapper.selectOne(any())).thenReturn(disabled);
        when(passwordEncoder.matches("password", "hash"))
                .thenReturn(true);

        MemberLoginDTO dto = new MemberLoginDTO();
        dto.setUsername("disabled-user");
        dto.setPassword("password");

        UmsMemberServiceImpl service = new UmsMemberServiceImpl(
                memberMapper,
                passwordEncoder,
                jwtTokenService
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.login(dto)
        );

        assertSame(ErrorCode.MEMBER_DISABLED, exception.getErrorCode());
        assertFalse(new MemberDetails(disabled).isEnabled());
        verify(jwtTokenService, never()).generateToken(disabled);
    }
}
