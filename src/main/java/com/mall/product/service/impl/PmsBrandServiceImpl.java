package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.BrandCreateDTO;
import com.mall.product.dto.BrandUpdateDTO;
import com.mall.product.entity.PmsBrand;
import com.mall.product.mapper.PmsBrandMapper;
import com.mall.product.service.PmsBrandService;
import com.mall.product.vo.BrandVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PmsBrandServiceImpl implements PmsBrandService {

    private final PmsBrandMapper brandMapper;

    @Override
    public PageResult<BrandVO> page(
            String keyword,
            int pageNum,
            int pageSize) {

        LambdaQueryWrapper<PmsBrand> query =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            query.like(
                    PmsBrand::getName,
                    keyword.trim()
            );
        }

        query.orderByDesc(PmsBrand::getSort)
                .orderByAsc(PmsBrand::getId);

        Page<PmsBrand> result = brandMapper.selectPage(
                new Page<>(pageNum, pageSize),
                query
        );

        return PageResult.from(
                result,
                BrandVO::from
        );
    }

    @Override
    public BrandVO create(BrandCreateDTO dto) {
        String name = dto.getName().trim();

        if (brandMapper.selectCount(
                new LambdaQueryWrapper<PmsBrand>()
                        .eq(PmsBrand::getName, name)
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.BRAND_NAME_ALREADY_EXISTS
            );
        }

        PmsBrand brand = new PmsBrand();

        brand.setName(name);
        brand.setFirstLetter(
                StringUtils.hasText(dto.getFirstLetter())
                        ? dto.getFirstLetter()
                        .toUpperCase(Locale.ROOT)
                        : null
        );
        brand.setSort(dto.getSort() == null ? 0 : dto.getSort());
        brand.setFactoryStatus(
                dto.getFactoryStatus() == null
                        ? 0
                        : dto.getFactoryStatus()
        );
        brand.setShowStatus(
                dto.getShowStatus() == null
                        ? 1
                        : dto.getShowStatus()
        );
        brand.setProductCount(0);
        brand.setProductCommentCount(0);
        brand.setLogo(normalizeText(dto.getLogo()));
        brand.setBigPic(normalizeText(dto.getBigPic()));
        brand.setBrandStory(normalizeText(dto.getBrandStory()));

        try {
            brandMapper.insert(brand);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.BRAND_NAME_ALREADY_EXISTS,
                    exception
            );
        }

        return BrandVO.from(
                brandMapper.selectById(brand.getId())
        );
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    @Override
    public BrandVO update(
            Long brandId,
            BrandUpdateDTO dto) {

        findById(brandId);

        LambdaUpdateWrapper<PmsBrand> wrapper =
                new LambdaUpdateWrapper<PmsBrand>()
                        .eq(PmsBrand::getId, brandId);

        boolean hasUpdate = false;

        if (dto.getName() != null) {
            String name = dto.getName().trim();

            if (brandMapper.selectCount(
                    new LambdaQueryWrapper<PmsBrand>()
                            .eq(PmsBrand::getName, name)
                            .ne(PmsBrand::getId, brandId)
            ) > 0) {
                throw new BusinessException(
                        ErrorCode.BRAND_NAME_ALREADY_EXISTS
                );
            }

            wrapper.set(PmsBrand::getName, name);
            hasUpdate = true;
        }

        if (dto.getFirstLetter() != null) {
            wrapper.set(
                    PmsBrand::getFirstLetter,
                    dto.getFirstLetter().toUpperCase(Locale.ROOT)
            );
            hasUpdate = true;
        }

        if (dto.getSort() != null) {
            wrapper.set(PmsBrand::getSort, dto.getSort());
            hasUpdate = true;
        }

        if (dto.getFactoryStatus() != null) {
            wrapper.set(
                    PmsBrand::getFactoryStatus,
                    dto.getFactoryStatus()
            );
            hasUpdate = true;
        }

        if (dto.getShowStatus() != null) {
            wrapper.set(
                    PmsBrand::getShowStatus,
                    dto.getShowStatus()
            );
            hasUpdate = true;
        }

        if (dto.getLogo() != null) {
            wrapper.set(
                    PmsBrand::getLogo,
                    normalizeText(dto.getLogo())
            );
            hasUpdate = true;
        }

        if (dto.getBigPic() != null) {
            wrapper.set(
                    PmsBrand::getBigPic,
                    normalizeText(dto.getBigPic())
            );
            hasUpdate = true;
        }

        if (dto.getBrandStory() != null) {
            wrapper.set(
                    PmsBrand::getBrandStory,
                    normalizeText(dto.getBrandStory())
            );
            hasUpdate = true;
        }

        if (hasUpdate) {
            try {
                brandMapper.update(null, wrapper);
            } catch (DataIntegrityViolationException exception) {
                throw new BusinessException(
                        ErrorCode.BRAND_NAME_ALREADY_EXISTS,
                        exception
                );
            }
        }

        return BrandVO.from(findById(brandId));
    }

    private PmsBrand findById(Long brandId) {
        PmsBrand brand = brandMapper.selectById(brandId);

        if (brand == null) {
            throw new BusinessException(
                    ErrorCode.BRAND_NOT_FOUND
            );
        }

        return brand;
    }

    @Override
    public void delete(Long brandId) {
        int deleted;

        try {
            deleted = brandMapper.deleteById(brandId);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.BRAND_HAS_PRODUCTS,
                    exception
            );
        }

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.BRAND_NOT_FOUND
            );
        }
    }
    @Override
    public BrandVO getById(Long brandId) {
        return BrandVO.from(
                findById(brandId)
        );
    }
}
