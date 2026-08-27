package com.mall.seckill.service;

import com.mall.seckill.dto.SeckillSubmitDTO;
import com.mall.seckill.vo.SeckillOrderStatusVO;
import com.mall.seckill.vo.SeckillSubmitVO;

public interface SeckillPortalService {

    SeckillSubmitVO submit(
            Long memberId,
            Long seckillSkuId,
            SeckillSubmitDTO dto
    );

    SeckillOrderStatusVO queryStatus(
            Long memberId,
            Long seckillSkuId,
            String requestId
    );
}