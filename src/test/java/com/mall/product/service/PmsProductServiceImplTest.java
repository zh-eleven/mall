package com.mall.product.service;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductCreateDTO;
import com.mall.product.dto.ProductUpdateDTO;
import com.mall.product.cache.PortalProductNotFoundCache;
import com.mall.product.entity.PmsBrand;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.mapper.*;
import com.mall.product.service.impl.PmsProductServiceImpl;
import com.mall.product.vo.ProductVO;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mall.product.entity.PmsProductAttributeValue;
import com.mall.product.entity.PmsSkuStock;

@ExtendWith(MockitoExtension.class)
class PmsProductServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                PmsProduct.class,
                PmsProductAttributeValue.class,
                PmsSkuStock.class
        );
    }

    @Mock
    private PmsProductMapper productMapper;

    @Mock
    private PmsBrandMapper brandMapper;

    @Mock
    private PmsProductCategoryMapper categoryMapper;

    private PmsProductServiceImpl productService;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @Mock
    private PmsProductAttributeValueMapper attributeValueMapper;

    @Mock
    private PortalProductNotFoundCache productNotFoundCache;

    @BeforeEach
    void setUp() {
        productService = new PmsProductServiceImpl(
                productMapper,
                brandMapper,
                categoryMapper,
                skuStockMapper,
                attributeValueMapper,
                productNotFoundCache
        );
    }

    @Test
    void createShouldValidateReferencesNormalizeSnAndSetDefaults() {
        ProductCreateDTO dto = createDto();
        dto.setBrandId(5L);
        dto.setName("  测试手机  ");
        dto.setProductSn("  phone-a_01  ");
        dto.setSubTitle("   ");
        when(brandMapper.selectById(5L)).thenReturn(brand(5L));
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(productMapper.insert(any(PmsProduct.class)))
                .thenAnswer(invocation -> {
                    PmsProduct value = invocation.getArgument(0);
                    value.setId(100L);
                    return 1;
                });
        when(productMapper.selectById(100L))
                .thenAnswer(invocation -> product(
                        100L,
                        "PHONE-A_01",
                        0
                ));

        var result = productService.create(dto);

        ArgumentCaptor<PmsProduct> captor =
                ArgumentCaptor.forClass(PmsProduct.class);
        verify(productMapper).insert(captor.capture());
        PmsProduct inserted = captor.getValue();
        assertEquals("测试手机", inserted.getName());
        assertEquals("PHONE-A_01", inserted.getProductSn());
        assertNull(inserted.getSubTitle());
        assertEquals(0, inserted.getStock());
        assertEquals(0, inserted.getLowStock());
        assertEquals(0, inserted.getPublishStatus());
        assertEquals(0, inserted.getNewStatus());
        assertEquals(0, inserted.getRecommendStatus());
        assertEquals(0, inserted.getVerifyStatus());
        assertEquals(0, inserted.getSort());
        assertEquals(0, inserted.getDeleteStatus());
        assertEquals(100L, result.id());
    }

    @Test
    void createDtoShouldAllowWhitespaceThatServiceNormalizesFromSn() {
        ProductCreateDTO dto = createDto();
        dto.setProductSn("  phone-01  ");

        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(dto).isEmpty());
        }
    }

    @Test
    void createShouldRejectMissingBrand() {
        ProductCreateDTO dto = createDto();
        dto.setBrandId(404L);
        when(brandMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.create(dto)
        );

        assertSame(ErrorCode.BRAND_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(categoryMapper, productMapper);
    }

    @Test
    void createShouldRejectFirstLevelCategory() {
        ProductCreateDTO dto = createDto();
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.create(dto)
        );

        assertSame(
                ErrorCode.PRODUCT_CATEGORY_INVALID,
                exception.getErrorCode()
        );
        verify(productMapper, never()).insert(any(PmsProduct.class));
    }

    @Test
    void createShouldRejectMarketPriceBelowSalePrice() {
        ProductCreateDTO dto = createDto();
        dto.setPrice(new BigDecimal("199.00"));
        dto.setOriginalPrice(new BigDecimal("198.99"));
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.create(dto)
        );

        assertSame(ErrorCode.PRODUCT_PRICE_INVALID, exception.getErrorCode());
        verify(productMapper, never()).selectCount(any());
        verify(productMapper, never()).insert(any(PmsProduct.class));
    }

    @Test
    void createShouldRejectNormalizedDuplicateSn() {
        ProductCreateDTO dto = createDto();
        dto.setProductSn(" phone-01 ");
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(productMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.create(dto)
        );

        assertSame(
                ErrorCode.PRODUCT_SN_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(productMapper, never()).insert(any(PmsProduct.class));
    }

    @Test
    void createShouldTranslateDuplicateKeyRaceAsDuplicateSn() {
        ProductCreateDTO dto = createDto();
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(productMapper.insert(any(PmsProduct.class)))
                .thenThrow(new DuplicateKeyException("duplicate key"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.create(dto)
        );

        assertSame(
                ErrorCode.PRODUCT_SN_ALREADY_EXISTS,
                exception.getErrorCode()
        );
    }

    @Test
    void createShouldNotMisreportOtherIntegrityFailureAsDuplicateSn() {
        ProductCreateDTO dto = createDto();
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("foreign key violation");
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(productMapper.insert(any(PmsProduct.class)))
                .thenThrow(databaseFailure);

        DataIntegrityViolationException actual = assertThrows(
                DataIntegrityViolationException.class,
                () -> productService.create(dto)
        );

        assertSame(databaseFailure, actual);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void pageShouldApplyAllFiltersAndConvertRecords() {
        Page<PmsProduct> databasePage = new Page<>(2, 5, 1);
        databasePage.setRecords(List.of(product(1L, "PHONE-01", 0)));
        when(productMapper.selectPage(any(Page.class), any()))
                .thenReturn(databasePage);

        var result = productService.page(
                " phone ",
                5L,
                20L,
                1,
                2,
                5
        );

        assertEquals(1, result.total());
        assertEquals("PHONE-01", result.list().getFirst().productSn());
        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper> queryCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(productMapper).selectPage(
                pageCaptor.capture(),
                queryCaptor.capture()
        );
        assertEquals(2, pageCaptor.getValue().getCurrent());
        assertEquals(5, pageCaptor.getValue().getSize());
        String sql = queryCaptor.getValue().getSqlSegment();
        Map<String, Object> values =
                queryCaptor.getValue().getParamNameValuePairs();
        assertTrue(values.values().stream().anyMatch(
                value -> value instanceof String text
                        && text.contains("phone")
        ));
        assertTrue(values.containsValue(5L));
        assertTrue(values.containsValue(20L));
        assertTrue(values.containsValue(1));
        assertTrue(sql.contains("brand_id"));
        assertTrue(sql.contains("product_category_id"));
        assertTrue(sql.contains("publish_status"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void updateShouldApplyOnlyProvidedFieldsAndClearBlankText() {
        PmsProduct current = product(10L, "OLD-01", 0);
        current.setDescription("旧描述");
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setName("  新名称  ");
        dto.setDescription("   ");
        when(productMapper.selectById(10L)).thenReturn(current);
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(productMapper.update(isNull(), any())).thenReturn(1);

        var result = productService.update(10L, dto);

        assertEquals("OLD-01", result.productSn());
        ArgumentCaptor<LambdaUpdateWrapper> captor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(productMapper).update(isNull(), captor.capture());
        String setSql = captor.getValue().getSqlSet();
        assertTrue(setSql.contains("name"));
        assertTrue(setSql.contains("description"));
        assertFalse(setSql.contains("product_sn"));
        assertFalse(setSql.contains("brand_id"));
    }

    @Test
    void updateWithEmptyRequestShouldBeNoOp() {
        PmsProduct current = product(10L, "OLD-01", 0);
        when(productMapper.selectById(10L)).thenReturn(current);

        var result = productService.update(10L, new ProductUpdateDTO());

        assertEquals(10L, result.id());
        verify(productMapper, never()).update(any(), any());
        verifyNoInteractions(brandMapper, categoryMapper);
        verify(productMapper, never()).selectCount(any());
    }

    @Test
    void updateShouldValidatePriceAgainstUnchangedOtherPrice() {
        PmsProduct current = product(10L, "OLD-01", 0);
        current.setPrice(new BigDecimal("100.00"));
        current.setOriginalPrice(new BigDecimal("120.00"));
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setPrice(new BigDecimal("130.00"));
        when(productMapper.selectById(10L)).thenReturn(current);
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.update(10L, dto)
        );

        assertSame(ErrorCode.PRODUCT_PRICE_INVALID, exception.getErrorCode());
        verify(productMapper, never()).update(any(), any());
    }

    @Test
    void deleteShouldSoftDeleteUnpublishedProduct() {
        when(productMapper.selectById(10L))
                .thenReturn(product(10L, "PHONE-01", 0));
        when(productMapper.deleteById(10L)).thenReturn(1);

        productService.delete(10L);

        verify(productMapper).deleteById(10L);
    }

    @Test
    void deleteShouldRejectPublishedProduct() {
        when(productMapper.selectById(10L))
                .thenReturn(product(10L, "PHONE-01", 1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.delete(10L)
        );

        assertSame(
                ErrorCode.PRODUCT_PUBLISHED_DELETE_FORBIDDEN,
                exception.getErrorCode()
        );
        verify(productMapper, never()).deleteById(anyLong());
    }

    @Test
    void deletedOrMissingProductShouldNotBeUpdatedOrDeletedAgain() {
        when(productMapper.selectById(10L)).thenReturn(null);

        BusinessException updateException = assertThrows(
                BusinessException.class,
                () -> productService.update(10L, new ProductUpdateDTO())
        );
        BusinessException deleteException = assertThrows(
                BusinessException.class,
                () -> productService.delete(10L)
        );

        assertSame(ErrorCode.PRODUCT_NOT_FOUND, updateException.getErrorCode());
        assertSame(ErrorCode.PRODUCT_NOT_FOUND, deleteException.getErrorCode());
        verify(productMapper, never()).update(any(), any());
        verify(productMapper, never()).deleteById(anyLong());
    }

    @Test
    void entityShouldConfigureDeleteStatusAsLogicalDelete() throws Exception {
        TableLogic tableLogic = PmsProduct.class
                .getDeclaredField("deleteStatus")
                .getAnnotation(TableLogic.class);

        assertNotNull(tableLogic);
        assertEquals("0", tableLogic.value());
        assertEquals("1", tableLogic.delval());
    }

    private ProductCreateDTO createDto() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setProductCategoryId(20L);
        dto.setName("测试商品");
        dto.setProductSn("PHONE-01");
        dto.setPrice(new BigDecimal("100.00"));
        dto.setOriginalPrice(new BigDecimal("120.00"));
        return dto;
    }

    private PmsBrand brand(Long id) {
        PmsBrand brand = new PmsBrand();
        brand.setId(id);
        brand.setName("品牌" + id);
        return brand;
    }

    private PmsProductCategory category(Long id, int level) {
        PmsProductCategory category = new PmsProductCategory();
        category.setId(id);
        category.setParentId(level == 0 ? 0L : 1L);
        category.setName("分类" + id);
        category.setLevel(level);
        return category;
    }

    private PmsProduct product(Long id, String productSn, int publishStatus) {
        PmsProduct product = new PmsProduct();
        product.setId(id);
        product.setBrandId(null);
        product.setProductCategoryId(20L);
        product.setName("测试商品");
        product.setProductSn(productSn);
        product.setPrice(new BigDecimal("100.00"));
        product.setOriginalPrice(new BigDecimal("120.00"));
        product.setStock(0);
        product.setLowStock(0);
        product.setPublishStatus(publishStatus);
        product.setNewStatus(0);
        product.setRecommendStatus(0);
        product.setVerifyStatus(0);
        product.setSort(0);
        product.setDeleteStatus(0);
        return product;
    }

    @Test
    void publishShouldRejectProductWithoutSku() {
        when(productMapper.selectById(10L))
                .thenReturn(product(10L, "PHONE-01", 0));
        when(skuStockMapper.selectCount(any()))
                .thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.updatePublishStatus(10L, 1)
        );

        assertSame(
                ErrorCode.PRODUCT_SKU_REQUIRED,
                exception.getErrorCode()
        );
        verify(productMapper, never()).update(isNull(), any());
    }

    @Test
    void publishShouldSucceedWhenProductHasSku() {
        PmsProduct product = product(10L, "PHONE-01", 0);

        when(productMapper.selectById(10L))
                .thenReturn(product);
        when(skuStockMapper.selectCount(any()))
                .thenReturn(2L);
        when(productMapper.update(isNull(), any()))
                .thenAnswer(invocation -> {
                    product.setPublishStatus(1);
                    return 1;
                });

        ProductVO result =
                productService.updatePublishStatus(10L, 1);

        assertEquals(1, result.publishStatus());
        verify(skuStockMapper).selectCount(any());
        verify(productMapper).update(isNull(), any());
        verify(productNotFoundCache).evictAfterCommit(10L);
    }

    @Test
    void unpublishShouldNotCheckSku() {
        PmsProduct product = product(10L, "PHONE-01", 1);

        when(productMapper.selectById(10L))
                .thenReturn(product);
        when(productMapper.update(isNull(), any()))
                .thenAnswer(invocation -> {
                    product.setPublishStatus(0);
                    return 1;
                });

        ProductVO result =
                productService.updatePublishStatus(10L, 0);

        assertEquals(0, result.publishStatus());
        verifyNoInteractions(skuStockMapper);
        verify(productMapper).update(isNull(), any());
        verify(productNotFoundCache, never())
                .evictAfterCommit(anyLong());
    }
    @Test
    void getDetailShouldReturnProductAttributeValuesAndSkus() {
        PmsProduct product = product(10L, "PHONE-01", 0);

        PmsProductAttributeValue attributeValue =
                new PmsProductAttributeValue();
        attributeValue.setId(20L);
        attributeValue.setProductId(10L);
        attributeValue.setProductAttributeId(2L);
        attributeValue.setValue("麒麟9000S");

        PmsSkuStock sku = new PmsSkuStock();
        sku.setId(30L);
        sku.setProductId(10L);
        sku.setSkuCode("PHONE-01-BLACK");
        sku.setPrice(new BigDecimal("100.00"));
        sku.setStock(10);
        sku.setLockedStock(0);
        sku.setLowStock(2);
        sku.setSpecKey("3=黑色");
        sku.setSpecData(
                "[{\"attributeId\":3,\"name\":\"颜色\",\"value\":\"黑色\"}]"
        );

        when(productMapper.selectById(10L))
                .thenReturn(product);
        when(attributeValueMapper.selectList(any()))
                .thenReturn(List.of(attributeValue));
        when(skuStockMapper.selectList(any()))
                .thenReturn(List.of(sku));

        var result = productService.getDetail(10L);

        assertEquals(10L, result.product().id());

        assertEquals(1, result.attributeValues().size());
        assertEquals(
                "麒麟9000S",
                result.attributeValues().getFirst().getValue()
        );

        assertEquals(1, result.skus().size());
        assertEquals(
                "PHONE-01-BLACK",
                result.skus().getFirst().getSkuCode()
        );
    }
    @Test
    void updateShouldRejectPriceChangeWhenProductHasSku() {
        PmsProduct current = product(10L, "PHONE-01", 0);

        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setPrice(new BigDecimal("99.00"));

        when(productMapper.selectById(10L))
                .thenReturn(current);
        when(skuStockMapper.selectCount(any()))
                .thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.update(10L, dto)
        );

        assertSame(
                ErrorCode.PRODUCT_SKU_DATA_UPDATE_FORBIDDEN,
                exception.getErrorCode()
        );
        verify(productMapper, never()).update(isNull(), any());
    }
    @Test
    void updateShouldRejectStockChangeWhenProductHasSku() {
        PmsProduct current = product(10L, "PHONE-01", 0);

        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStock(100);

        when(productMapper.selectById(10L))
                .thenReturn(current);
        when(skuStockMapper.selectCount(any()))
                .thenReturn(2L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.update(10L, dto)
        );

        assertSame(
                ErrorCode.PRODUCT_SKU_DATA_UPDATE_FORBIDDEN,
                exception.getErrorCode()
        );
        verify(productMapper, never()).update(isNull(), any());
    }
    @Test
    void updateShouldRejectCategoryChangeWhenProductHasAttributeValues() {
        PmsProduct current = product(10L, "PHONE-01", 0);

        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setProductCategoryId(30L);

        when(productMapper.selectById(10L))
                .thenReturn(current);
        when(skuStockMapper.selectCount(any()))
                .thenReturn(0L);
        when(attributeValueMapper.selectCount(any()))
                .thenReturn(2L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.update(10L, dto)
        );

        assertSame(
                ErrorCode.PRODUCT_CATEGORY_CHANGE_FORBIDDEN,
                exception.getErrorCode()
        );
        verify(categoryMapper, never()).selectById(anyLong());
        verify(productMapper, never()).update(isNull(), any());
    }
}
