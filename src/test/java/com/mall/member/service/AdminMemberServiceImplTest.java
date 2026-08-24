package com.mall.member.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.dto.AdminMemberQueryDTO;
import com.mall.member.dto.MemberStatusUpdateDTO;
import com.mall.member.entity.UmsMember;
import com.mall.member.mapper.UmsMemberMapper;
import com.mall.member.service.impl.AdminMemberServiceImpl;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(UmsMember.class);
    }

    @Mock
    private UmsMemberMapper memberMapper;

    @Test
    void pageShouldApplyFiltersPaginationAndSafeMapping() {
        AdminMemberServiceImpl service = service();
        Page<UmsMember> data = new Page<>(2, 20);
        data.setTotal(1);
        data.setRecords(List.of(member(7L, 1)));

        when(memberMapper.selectPage(any(), any()))
                .thenReturn(data);

        AdminMemberQueryDTO query = new AdminMemberQueryDTO();
        query.setUsername(" alice ");
        query.setPhone(" 138 ");
        query.setStatus(1);
        query.setPageNum(2);
        query.setPageSize(20);

        var result = service.page(query);

        assertEquals(2, result.pageNum());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.total());
        assertEquals("alice", result.list().getFirst().username());

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Wrapper> wrapperCaptor =
                ArgumentCaptor.forClass(Wrapper.class);
        verify(memberMapper).selectPage(
                any(),
                wrapperCaptor.capture()
        );

        Wrapper<?> wrapper = wrapperCaptor.getValue();
        assertTrue(wrapper.getSqlSegment().contains("username"));
        assertTrue(wrapper.getSqlSegment().contains("phone"));
        assertTrue(wrapper.getSqlSegment().contains("status"));
        assertTrue(wrapper.getSqlSegment().contains("create_time"));
        assertTrue(wrapper.getSqlSegment().contains("id"));
        AbstractWrapper<?, ?, ?> abstractWrapper =
                (AbstractWrapper<?, ?, ?>) wrapper;
        assertTrue(abstractWrapper.getParamNameValuePairs()
                .containsValue("%alice%"));
        assertTrue(abstractWrapper.getParamNameValuePairs()
                .containsValue("%138%"));
        assertFalse(AdminMemberVOFields.containsPassword());
    }

    @Test
    void detailShouldRejectMissingMember() {
        AdminMemberServiceImpl service = service();
        when(memberMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getDetail(99L)
        );

        assertSame(
                ErrorCode.MEMBER_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void updateStatusShouldDisableAndEnableMember() {
        AdminMemberServiceImpl service = service();
        UmsMember enabled = member(7L, 1);
        UmsMember disabled = member(7L, 0);

        when(memberMapper.selectById(7L))
                .thenReturn(enabled, disabled, disabled, enabled);
        when(memberMapper.updateStatusIfCurrent(7L, 1, 0))
                .thenReturn(1);
        when(memberMapper.updateStatusIfCurrent(7L, 0, 1))
                .thenReturn(1);

        assertEquals(0, service.updateStatus(
                7L,
                status(0)
        ).status());
        assertEquals(1, service.updateStatus(
                7L,
                status(1)
        ).status());
    }

    @Test
    void updateStatusShouldRejectIllegalStatus() {
        AdminMemberServiceImpl service = service();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateStatus(7L, status(2))
        );

        assertSame(
                ErrorCode.MEMBER_STATUS_INVALID,
                exception.getErrorCode()
        );
        verify(memberMapper, never()).selectById(any());
    }

    @Test
    void pageShouldRejectIllegalStatus() {
        AdminMemberServiceImpl service = service();
        AdminMemberQueryDTO query = new AdminMemberQueryDTO();
        query.setStatus(-1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.page(query)
        );

        assertSame(
                ErrorCode.MEMBER_STATUS_INVALID,
                exception.getErrorCode()
        );
    }

    @Test
    void updateStatusShouldRejectConcurrentChange() {
        AdminMemberServiceImpl service = service();
        when(memberMapper.selectById(7L))
                .thenReturn(member(7L, 1));
        when(memberMapper.updateStatusIfCurrent(7L, 1, 0))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateStatus(7L, status(0))
        );

        assertSame(
                ErrorCode.MEMBER_CONCURRENT_OPERATION,
                exception.getErrorCode()
        );
    }

    private AdminMemberServiceImpl service() {
        return new AdminMemberServiceImpl(memberMapper);
    }

    private MemberStatusUpdateDTO status(int status) {
        MemberStatusUpdateDTO dto = new MemberStatusUpdateDTO();
        dto.setStatus(status);
        return dto;
    }

    private UmsMember member(Long id, int status) {
        UmsMember member = new UmsMember();
        member.setId(id);
        member.setUsername("alice");
        member.setPassword("secret-hash");
        member.setPhone("13800000000");
        member.setStatus(status);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        return member;
    }

    private static final class AdminMemberVOFields {
        private static boolean containsPassword() {
            return java.util.Arrays.stream(
                            com.mall.member.vo.AdminMemberVO.class
                                    .getRecordComponents()
                    )
                    .anyMatch(component ->
                            component.getName().equals("password")
                    );
        }
    }
}
