package com.mall.seckill.controller;

import com.mall.common.api.ApiResult;
import com.mall.security.MemberDetails;
import com.mall.seckill.dto.SeckillSubmitDTO;
import com.mall.seckill.service.SeckillPortalService;
import com.mall.seckill.vo.SeckillSubmitVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation
        .AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mall.seckill.vo.SeckillOrderStatusVO;
import jakarta.validation.constraints.Pattern;


@RestController
@RequestMapping("/api/members/me/seckill")
@RequiredArgsConstructor
@Validated
public class SeckillPortalController {

    private final SeckillPortalService seckillPortalService;

    @PostMapping("/{seckillSkuId}/orders")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResult<SeckillSubmitVO> submit(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "秒杀SKU ID必须大于0")
            Long seckillSkuId,

            @Valid @RequestBody
            SeckillSubmitDTO dto) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                seckillPortalService.submit(
                        memberId,
                        seckillSkuId,
                        dto
                ),
                "秒杀请求已进入队列"
        );
    }

    @GetMapping(
            "/{seckillSkuId}/orders/{requestId}"
    )
    public ApiResult<SeckillOrderStatusVO> queryStatus(
            @AuthenticationPrincipal
            MemberDetails memberDetails,

            @PathVariable
            @Positive(message = "秒杀SKU ID必须大于0")
            Long seckillSkuId,

            @PathVariable
            @Pattern(
                    regexp =
                            "^[0-9a-fA-F]{8}-"
                                    + "[0-9a-fA-F]{4}-"
                                    + "[1-5][0-9a-fA-F]{3}-"
                                    + "[89abAB][0-9a-fA-F]{3}-"
                                    + "[0-9a-fA-F]{12}$",
                    message = "请求ID必须是有效的UUID"
            )
            String requestId) {

        Long memberId =
                memberDetails.getMember().getId();

        return ApiResult.success(
                seckillPortalService.queryStatus(
                        memberId,
                        seckillSkuId,
                        requestId
                )
        );
    }
}