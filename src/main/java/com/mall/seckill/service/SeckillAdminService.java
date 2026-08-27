package com.mall.seckill.service;

import com.mall.seckill.dto.SeckillActivityCreateDTO;
import com.mall.seckill.vo.SeckillActivityVO;
import com.mall.seckill.dto.SeckillSkuCreateDTO;
import com.mall.seckill.vo.SeckillSkuVO;
import java.util.List;
public interface SeckillAdminService {

    SeckillActivityVO createActivity(
            SeckillActivityCreateDTO dto
    );

    SeckillActivityVO getActivity(Long activityId);

    SeckillSkuVO addSku(
            Long activityId,
            SeckillSkuCreateDTO dto
    );
    List<SeckillSkuVO> listSkus(Long activityId);
    SeckillActivityVO enableActivity(Long activityId);
}