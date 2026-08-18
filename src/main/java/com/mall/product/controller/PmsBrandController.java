package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.product.dto.BrandCreateDTO;
import com.mall.product.dto.BrandUpdateDTO;
import com.mall.product.service.PmsBrandService;
import com.mall.product.vo.BrandVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
@Validated
public class PmsBrandController {

    private final PmsBrandService brandService;

    @GetMapping
    @PreAuthorize("hasAuthority('brand:read')")
    public ApiResult<PageResult<BrandVO>> page(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码必须大于等于1")
            Integer pageNum,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于等于1")
            @Max(value = 100, message = "每页数量不能超过100")
            Integer pageSize,

            @RequestParam(required = false)
            @Size(max = 64, message = "关键词长度不能超过64个字符")
            String keyword) {

        return ApiResult.success(
                brandService.page(
                        keyword,
                        pageNum,
                        pageSize
                )
        );
    }
    @PostMapping
    @PreAuthorize("hasAuthority('brand:write')")
    public ApiResult<BrandVO> create(
            @Valid @RequestBody BrandCreateDTO dto) {

        return ApiResult.success(
                brandService.create(dto),
                "品牌创建成功"
        );
    }

    @PatchMapping("/{brandId}")
    @PreAuthorize("hasAuthority('brand:write')")
    public ApiResult<BrandVO> update(
            @PathVariable
            @Positive(message = "品牌ID必须大于0") Long brandId,
            @Valid @RequestBody BrandUpdateDTO dto) {

        return ApiResult.success(
                brandService.update(brandId, dto),
                "品牌修改成功"
        );
    }

    @DeleteMapping("/{brandId}")
    @PreAuthorize("hasAuthority('brand:write')")
    public ApiResult<Void> delete(
            @PathVariable
            @Positive(message = "品牌ID必须大于0") Long brandId) {

        brandService.delete(brandId);

        return ApiResult.success(
                null,
                "品牌删除成功"
        );
    }
    @GetMapping("/{brandId}")
    @PreAuthorize("hasAuthority('brand:read')")
    public ApiResult<BrandVO> getById(
            @PathVariable
            @Positive(message = "品牌ID必须大于0") Long brandId) {

        return ApiResult.success(
                brandService.getById(brandId)
        );
    }
}
