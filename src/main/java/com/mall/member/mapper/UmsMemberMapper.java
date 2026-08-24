package com.mall.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.member.entity.UmsMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UmsMemberMapper extends BaseMapper<UmsMember> {

    @Update("""
            UPDATE ums_member
            SET status = #{targetStatus},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{memberId}
              AND status = #{currentStatus}
            """)
    int updateStatusIfCurrent(
            @Param("memberId") Long memberId,
            @Param("currentStatus") Integer currentStatus,
            @Param("targetStatus") Integer targetStatus
    );
}
