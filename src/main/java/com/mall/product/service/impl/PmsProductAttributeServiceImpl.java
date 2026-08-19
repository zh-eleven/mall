package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductAttributeCreateDTO;
import com.mall.product.dto.ProductAttributeUpdateDTO;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeCategory;
import com.mall.product.mapper.PmsProductAttributeCategoryMapper;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.service.PmsProductAttributeService;
import com.mall.product.vo.ProductAttributeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PmsProductAttributeServiceImpl
        implements PmsProductAttributeService {

    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductAttributeCategoryMapper categoryMapper;

    @Override
    @Transactional
    public ProductAttributeVO create(ProductAttributeCreateDTO dto) {
        findCategory(dto.getProductAttributeCategoryId());

        String name = dto.getName().trim();
        int type = dto.getType();
        int inputType = defaultValue(dto.getInputType());
        String inputList = normalizeInputList(
                inputType,
                dto.getInputList()
        );

        ensureNameAvailable(
                dto.getProductAttributeCategoryId(),
                name,
                type,
                null
        );

        PmsProductAttribute attribute = new PmsProductAttribute();

        attribute.setProductAttributeCategoryId(
                dto.getProductAttributeCategoryId()
        );
        attribute.setName(name);
        attribute.setSelectType(defaultValue(dto.getSelectType()));
        attribute.setInputType(inputType);
        attribute.setInputList(inputList);
        attribute.setSort(defaultValue(dto.getSort()));
        attribute.setFilterType(defaultValue(dto.getFilterType()));
        attribute.setSearchType(defaultValue(dto.getSearchType()));
        attribute.setRelatedStatus(defaultValue(dto.getRelatedStatus()));
        attribute.setHandAddStatus(defaultValue(dto.getHandAddStatus()));
        attribute.setType(type);

        try {
            attributeMapper.insert(attribute);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS,
                    exception
            );
        }

        changeCount(
                attribute.getProductAttributeCategoryId(),
                attribute.getType(),
                1
        );

        return ProductAttributeVO.from(
                attributeMapper.selectById(attribute.getId())
        );
    }

    @Override
    public PageResult<ProductAttributeVO> page(
            Long categoryId,
            Integer type,
            String keyword,
            int pageNum,
            int pageSize) {

        LambdaQueryWrapper<PmsProductAttribute> query =
                new LambdaQueryWrapper<>();

        if (categoryId != null) {
            query.eq(
                    PmsProductAttribute::getProductAttributeCategoryId,
                    categoryId
            );
        }

        if (type != null) {
            query.eq(PmsProductAttribute::getType, type);
        }

        if (StringUtils.hasText(keyword)) {
            query.like(
                    PmsProductAttribute::getName,
                    keyword.trim()
            );
        }

        query.orderByDesc(PmsProductAttribute::getSort)
                .orderByAsc(PmsProductAttribute::getId);

        Page<PmsProductAttribute> result =
                attributeMapper.selectPage(
                        new Page<>(pageNum, pageSize),
                        query
                );

        return PageResult.from(
                result,
                ProductAttributeVO::from
        );
    }

    @Override
    public ProductAttributeVO getById(Long attributeId) {
        return ProductAttributeVO.from(findById(attributeId));
    }

    @Override
    @Transactional
    public ProductAttributeVO update(
            Long attributeId,
            ProductAttributeUpdateDTO dto) {

        PmsProductAttribute current = findById(attributeId);

        Long targetCategoryId =
                dto.getProductAttributeCategoryId() == null
                        ? current.getProductAttributeCategoryId()
                        : dto.getProductAttributeCategoryId();

        Integer targetType = dto.getType() == null
                ? current.getType()
                : dto.getType();

        String targetName = dto.getName() == null
                ? current.getName()
                : dto.getName().trim();

        Integer targetInputType = dto.getInputType() == null
                ? current.getInputType()
                : dto.getInputType();

        String targetInputList = normalizeInputList(
                targetInputType,
                dto.getInputList() == null
                        ? current.getInputList()
                        : dto.getInputList()
        );

        findCategory(targetCategoryId);

        ensureNameAvailable(
                targetCategoryId,
                targetName,
                targetType,
                attributeId
        );

        LambdaUpdateWrapper<PmsProductAttribute> wrapper =
                new LambdaUpdateWrapper<PmsProductAttribute>()
                        .eq(PmsProductAttribute::getId, attributeId)
                        .set(
                                PmsProductAttribute
                                        ::getProductAttributeCategoryId,
                                targetCategoryId
                        )
                        .set(PmsProductAttribute::getName, targetName)
                        .set(
                                PmsProductAttribute::getInputType,
                                targetInputType
                        )
                        .set(
                                PmsProductAttribute::getInputList,
                                targetInputList
                        )
                        .set(PmsProductAttribute::getType, targetType);

        if (dto.getSelectType() != null) {
            wrapper.set(
                    PmsProductAttribute::getSelectType,
                    dto.getSelectType()
            );
        }

        if (dto.getSort() != null) {
            wrapper.set(PmsProductAttribute::getSort, dto.getSort());
        }

        if (dto.getFilterType() != null) {
            wrapper.set(
                    PmsProductAttribute::getFilterType,
                    dto.getFilterType()
            );
        }

        if (dto.getSearchType() != null) {
            wrapper.set(
                    PmsProductAttribute::getSearchType,
                    dto.getSearchType()
            );
        }

        if (dto.getRelatedStatus() != null) {
            wrapper.set(
                    PmsProductAttribute::getRelatedStatus,
                    dto.getRelatedStatus()
            );
        }

        if (dto.getHandAddStatus() != null) {
            wrapper.set(
                    PmsProductAttribute::getHandAddStatus,
                    dto.getHandAddStatus()
            );
        }

        try {
            attributeMapper.update(null, wrapper);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS,
                    exception
            );
        }

        boolean categoryChanged = !current
                .getProductAttributeCategoryId()
                .equals(targetCategoryId);

        boolean typeChanged = !current.getType().equals(targetType);

        if (categoryChanged || typeChanged) {
            changeCount(
                    current.getProductAttributeCategoryId(),
                    current.getType(),
                    -1
            );

            changeCount(targetCategoryId, targetType, 1);
        }

        return ProductAttributeVO.from(findById(attributeId));
    }

    @Override
    @Transactional
    public void delete(Long attributeId) {
        PmsProductAttribute attribute = findById(attributeId);

        int deleted;

        try {
            deleted = attributeMapper.deleteById(attributeId);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_HAS_RELATIONS,
                    exception
            );
        }

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_NOT_FOUND
            );
        }

        changeCount(
                attribute.getProductAttributeCategoryId(),
                attribute.getType(),
                -1
        );
    }

    private PmsProductAttribute findById(Long attributeId) {
        PmsProductAttribute attribute =
                attributeMapper.selectById(attributeId);

        if (attribute == null) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_NOT_FOUND
            );
        }

        return attribute;
    }

    private PmsProductAttributeCategory findCategory(Long categoryId) {
        PmsProductAttributeCategory category =
                categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND
            );
        }

        return category;
    }

    private void ensureNameAvailable(
            Long categoryId,
            String name,
            Integer type,
            Long ignoredId) {

        LambdaQueryWrapper<PmsProductAttribute> query =
                new LambdaQueryWrapper<PmsProductAttribute>()
                        .eq(
                                PmsProductAttribute
                                        ::getProductAttributeCategoryId,
                                categoryId
                        )
                        .eq(PmsProductAttribute::getName, name)
                        .eq(PmsProductAttribute::getType, type);

        if (ignoredId != null) {
            query.ne(PmsProductAttribute::getId, ignoredId);
        }

        if (attributeMapper.selectCount(query) > 0) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS
            );
        }
    }

    private String normalizeInputList(
            Integer inputType,
            String inputList) {

        if (inputType == 1) {
            if (!StringUtils.hasText(inputList)) {
                throw new BusinessException(
                        ErrorCode.ATTRIBUTE_INPUT_LIST_REQUIRED
                );
            }

            String[] values = inputList.split(",", -1);

            for (int index = 0; index < values.length; index++) {
                values[index] = values[index].trim();

                if (values[index].isEmpty()) {
                    throw new BusinessException(
                            ErrorCode.ATTRIBUTE_INPUT_LIST_REQUIRED
                    );
                }
            }

            return String.join(",", values);
        }

        return null;
    }

    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private void changeCount(
            Long categoryId,
            Integer type,
            int change) {

        String sql;

        if (type == 0) {
            sql = change > 0
                    ? "attribute_count = attribute_count + 1"
                    : "attribute_count = GREATEST(attribute_count - 1, 0)";
        } else {
            sql = change > 0
                    ? "param_count = param_count + 1"
                    : "param_count = GREATEST(param_count - 1, 0)";
        }

        int updated = categoryMapper.update(
                null,
                new LambdaUpdateWrapper<PmsProductAttributeCategory>()
                        .eq(
                                PmsProductAttributeCategory::getId,
                                categoryId
                        )
                        .setSql(sql)
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND
            );
        }
    }
}
