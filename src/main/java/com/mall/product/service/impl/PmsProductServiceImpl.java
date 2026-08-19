package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductCreateDTO;
import com.mall.product.dto.ProductUpdateDTO;
import com.mall.product.entity.PmsBrand;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.mapper.PmsBrandMapper;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.service.PmsProductService;
import com.mall.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PmsProductServiceImpl implements PmsProductService {

    private final PmsProductMapper productMapper;
    private final PmsBrandMapper brandMapper;
    private final PmsProductCategoryMapper categoryMapper;

    @Override
    @Transactional
    public ProductVO create(ProductCreateDTO dto) {
        validateBrand(dto.getBrandId());
        validateCategory(dto.getProductCategoryId());
        validatePrice(dto.getPrice(), dto.getOriginalPrice());

        String productSn = normalizeProductSn(dto.getProductSn());
        ensureProductSnAvailable(productSn, null);

        PmsProduct product = new PmsProduct();

        product.setBrandId(dto.getBrandId());
        product.setProductCategoryId(dto.getProductCategoryId());
        product.setName(dto.getName().trim());
        product.setSubTitle(normalizeText(dto.getSubTitle()));
        product.setProductSn(productSn);
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(defaultValue(dto.getStock()));
        product.setLowStock(defaultValue(dto.getLowStock()));
        product.setUnit(normalizeText(dto.getUnit()));
        product.setWeight(dto.getWeight());
        product.setPublishStatus(defaultValue(dto.getPublishStatus()));
        product.setNewStatus(defaultValue(dto.getNewStatus()));
        product.setRecommendStatus(
                defaultValue(dto.getRecommendStatus())
        );
        product.setVerifyStatus(defaultValue(dto.getVerifyStatus()));
        product.setSort(defaultValue(dto.getSort()));
        product.setPic(normalizeText(dto.getPic()));
        product.setAlbumPics(normalizeText(dto.getAlbumPics()));
        product.setDescription(normalizeText(dto.getDescription()));
        product.setDetailTitle(normalizeText(dto.getDetailTitle()));
        product.setDetailDesc(normalizeText(dto.getDetailDesc()));
        product.setDetailHtml(normalizeText(dto.getDetailHtml()));
        product.setDeleteStatus(0);

        try {
            productMapper.insert(product);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_SN_ALREADY_EXISTS,
                    exception
            );
        }

        return ProductVO.from(
                productMapper.selectById(product.getId())
        );
    }

    @Override
    public PageResult<ProductVO> page(
            String keyword,
            Long brandId,
            Long categoryId,
            Integer publishStatus,
            int pageNum,
            int pageSize) {

        LambdaQueryWrapper<PmsProduct> query =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();

            query.and(wrapper -> wrapper
                    .like(PmsProduct::getName, value)
                    .or()
                    .like(PmsProduct::getProductSn, value)
            );
        }

        if (brandId != null) {
            query.eq(PmsProduct::getBrandId, brandId);
        }

        if (categoryId != null) {
            query.eq(
                    PmsProduct::getProductCategoryId,
                    categoryId
            );
        }

        if (publishStatus != null) {
            query.eq(
                    PmsProduct::getPublishStatus,
                    publishStatus
            );
        }

        query.orderByDesc(PmsProduct::getSort)
                .orderByDesc(PmsProduct::getId);

        Page<PmsProduct> result = productMapper.selectPage(
                new Page<>(pageNum, pageSize),
                query
        );

        return PageResult.from(result, ProductVO::from);
    }

    @Override
    public ProductVO getById(Long productId) {
        return ProductVO.from(findById(productId));
    }

    @Override
    @Transactional
    public ProductVO update(
            Long productId,
            ProductUpdateDTO dto) {

        PmsProduct current = findById(productId);

        if (!hasUpdates(dto)) {
            return ProductVO.from(current);
        }

        Long targetCategoryId =
                dto.getProductCategoryId() == null
                        ? current.getProductCategoryId()
                        : dto.getProductCategoryId();

        Long targetBrandId = dto.getBrandId() == null
                ? current.getBrandId()
                : dto.getBrandId();

        String targetProductSn = dto.getProductSn() == null
                ? current.getProductSn()
                : normalizeProductSn(dto.getProductSn());

        BigDecimal targetPrice = dto.getPrice() == null
                ? current.getPrice()
                : dto.getPrice();

        BigDecimal targetOriginalPrice =
                dto.getOriginalPrice() == null
                        ? current.getOriginalPrice()
                        : dto.getOriginalPrice();

        validateBrand(targetBrandId);
        validateCategory(targetCategoryId);
        validatePrice(targetPrice, targetOriginalPrice);
        ensureProductSnAvailable(targetProductSn, productId);

        LambdaUpdateWrapper<PmsProduct> wrapper =
                new LambdaUpdateWrapper<PmsProduct>()
                        .eq(PmsProduct::getId, productId);

        wrapper.set(
                dto.getBrandId() != null,
                PmsProduct::getBrandId,
                dto.getBrandId()
        );

        wrapper.set(
                dto.getProductCategoryId() != null,
                PmsProduct::getProductCategoryId,
                dto.getProductCategoryId()
        );

        wrapper.set(
                dto.getName() != null,
                PmsProduct::getName,
                dto.getName() == null
                        ? null
                        : dto.getName().trim()
        );

        wrapper.set(
                dto.getSubTitle() != null,
                PmsProduct::getSubTitle,
                normalizeText(dto.getSubTitle())
        );

        wrapper.set(
                dto.getProductSn() != null,
                PmsProduct::getProductSn,
                targetProductSn
        );

        wrapper.set(
                dto.getPrice() != null,
                PmsProduct::getPrice,
                dto.getPrice()
        );

        wrapper.set(
                dto.getOriginalPrice() != null,
                PmsProduct::getOriginalPrice,
                dto.getOriginalPrice()
        );

        wrapper.set(
                dto.getStock() != null,
                PmsProduct::getStock,
                dto.getStock()
        );

        wrapper.set(
                dto.getLowStock() != null,
                PmsProduct::getLowStock,
                dto.getLowStock()
        );

        wrapper.set(
                dto.getUnit() != null,
                PmsProduct::getUnit,
                normalizeText(dto.getUnit())
        );

        wrapper.set(
                dto.getWeight() != null,
                PmsProduct::getWeight,
                dto.getWeight()
        );

        wrapper.set(
                dto.getPublishStatus() != null,
                PmsProduct::getPublishStatus,
                dto.getPublishStatus()
        );

        wrapper.set(
                dto.getNewStatus() != null,
                PmsProduct::getNewStatus,
                dto.getNewStatus()
        );

        wrapper.set(
                dto.getRecommendStatus() != null,
                PmsProduct::getRecommendStatus,
                dto.getRecommendStatus()
        );

        wrapper.set(
                dto.getVerifyStatus() != null,
                PmsProduct::getVerifyStatus,
                dto.getVerifyStatus()
        );

        wrapper.set(
                dto.getSort() != null,
                PmsProduct::getSort,
                dto.getSort()
        );

        wrapper.set(
                dto.getPic() != null,
                PmsProduct::getPic,
                normalizeText(dto.getPic())
        );

        wrapper.set(
                dto.getAlbumPics() != null,
                PmsProduct::getAlbumPics,
                normalizeText(dto.getAlbumPics())
        );

        wrapper.set(
                dto.getDescription() != null,
                PmsProduct::getDescription,
                normalizeText(dto.getDescription())
        );

        wrapper.set(
                dto.getDetailTitle() != null,
                PmsProduct::getDetailTitle,
                normalizeText(dto.getDetailTitle())
        );

        wrapper.set(
                dto.getDetailDesc() != null,
                PmsProduct::getDetailDesc,
                normalizeText(dto.getDetailDesc())
        );

        wrapper.set(
                dto.getDetailHtml() != null,
                PmsProduct::getDetailHtml,
                normalizeText(dto.getDetailHtml())
        );

        try {
            productMapper.update(null, wrapper);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_SN_ALREADY_EXISTS,
                    exception
            );
        }

        return ProductVO.from(findById(productId));
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        PmsProduct product = findById(productId);

        if (Integer.valueOf(1).equals(product.getPublishStatus())) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_PUBLISHED_DELETE_FORBIDDEN
            );
        }

        int deleted = productMapper.deleteById(productId);

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }
    }

    private PmsProduct findById(Long productId) {
        PmsProduct product = productMapper.selectById(productId);

        if (product == null) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private void validateBrand(Long brandId) {
        if (brandId == null) {
            return;
        }

        PmsBrand brand = brandMapper.selectById(brandId);

        if (brand == null) {
            throw new BusinessException(
                    ErrorCode.BRAND_NOT_FOUND
            );
        }
    }

    private void validateCategory(Long categoryId) {
        PmsProductCategory category =
                categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND
            );
        }

        if (!Integer.valueOf(1).equals(category.getLevel())) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_CATEGORY_INVALID
            );
        }
    }

    private void validatePrice(
            BigDecimal price,
            BigDecimal originalPrice) {

        if (originalPrice != null
                && originalPrice.compareTo(price) < 0) {

            throw new BusinessException(
                    ErrorCode.PRODUCT_PRICE_INVALID
            );
        }
    }

    private void ensureProductSnAvailable(
            String productSn,
            Long ignoredId) {

        LambdaQueryWrapper<PmsProduct> query =
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getProductSn, productSn);

        if (ignoredId != null) {
            query.ne(PmsProduct::getId, ignoredId);
        }

        if (productMapper.selectCount(query) > 0) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_SN_ALREADY_EXISTS
            );
        }
    }

    private String normalizeProductSn(String productSn) {
        return productSn.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasUpdates(ProductUpdateDTO dto) {
        return dto.getBrandId() != null
                || dto.getProductCategoryId() != null
                || dto.getName() != null
                || dto.getSubTitle() != null
                || dto.getProductSn() != null
                || dto.getPrice() != null
                || dto.getOriginalPrice() != null
                || dto.getStock() != null
                || dto.getLowStock() != null
                || dto.getUnit() != null
                || dto.getWeight() != null
                || dto.getPublishStatus() != null
                || dto.getNewStatus() != null
                || dto.getRecommendStatus() != null
                || dto.getVerifyStatus() != null
                || dto.getSort() != null
                || dto.getPic() != null
                || dto.getAlbumPics() != null
                || dto.getDescription() != null
                || dto.getDetailTitle() != null
                || dto.getDetailDesc() != null
                || dto.getDetailHtml() != null;
    }
}
