package com.mall.seckill.controller;

import com.mall.common.api.ApiResult;
import com.mall.seckill.dto.SeckillActivityCreateDTO;
import com.mall.seckill.service.SeckillAdminService;
import com.mall.seckill.vo.SeckillActivityVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mall.seckill.dto.SeckillSkuCreateDTO;
import com.mall.seckill.vo.SeckillSkuVO;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seckill")
@RequiredArgsConstructor
@Validated
public class SeckillAdminController {

    private final SeckillAdminService seckillAdminService;

    @PostMapping("/activities")
    @PreAuthorize("hasAuthority('seckill:write')")
    public ApiResult<SeckillActivityVO> createActivity(
            @Valid @RequestBody
            SeckillActivityCreateDTO dto) {

        return ApiResult.success(
                seckillAdminService.createActivity(dto),
                "秒杀活动创建成功"
        );
    }

    @GetMapping("/activities/{activityId}")
    @PreAuthorize("hasAuthority('seckill:read')")
    public ApiResult<SeckillActivityVO> getActivity(
            @PathVariable
            @Positive(message = "活动ID必须大于0")
            Long activityId) {

        return ApiResult.success(
                seckillAdminService.getActivity(activityId)
        );
    }

    @PostMapping("/activities/{activityId}/skus")
    @PreAuthorize("hasAuthority('seckill:write')")
    public ApiResult<SeckillSkuVO> addSku(
            @PathVariable
            @Positive(message = "活动ID必须大于0")
            Long activityId,

            @Valid @RequestBody
            SeckillSkuCreateDTO dto) {

        return ApiResult.success(
                seckillAdminService.addSku(
                        activityId,
                        dto
                ),
                "秒杀商品添加成功"
        );
    }

    @GetMapping("/activities/{activityId}/skus")
    @PreAuthorize("hasAuthority('seckill:read')")
    public ApiResult<List<SeckillSkuVO>> listSkus(
            @PathVariable
            @Positive(message = "活动ID必须大于0")
            Long activityId) {

        return ApiResult.success(
                seckillAdminService.listSkus(activityId)
        );
    }

    @PatchMapping("/activities/{activityId}/enable")
    @PreAuthorize("hasAuthority('seckill:write')")
    public ApiResult<SeckillActivityVO> enableActivity(
            @PathVariable
            @Positive(message = "活动ID必须大于0")
            Long activityId) {

        return ApiResult.success(
                seckillAdminService.enableActivity(activityId),
                "秒杀活动启用成功"
        );
    }
}