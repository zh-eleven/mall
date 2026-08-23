package com.mall.portal.order.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.member.entity.UmsMemberReceiveAddress;
import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.portal.order.entity.OmsCartItem;
import com.mall.portal.order.mapper.OmsCartItemMapper;
import com.mall.portal.order.dto.OrderPreviewDTO;
import com.mall.portal.order.service.impl.PortalOrderServiceImpl;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalOrderServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                UmsMemberReceiveAddress.class,
                OmsCartItem.class,
                PmsProduct.class,
                PmsSkuStock.class
        );
    }

    @Mock
    private UmsMemberReceiveAddressMapper addressMapper;

    @Mock
    private OmsCartItemMapper cartItemMapper;

    @Mock
    private PmsProductMapper productMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @InjectMocks
    private PortalOrderServiceImpl orderService;

    @Test
    void previewShouldReturnReceiverItemsAndAmount() {

        UmsMemberReceiveAddress address = address();
        OmsCartItem cartItem = cartItem();
        PmsProduct product = product();
        PmsSkuStock sku = sku();

        stubPreviewData(
                address,
                cartItem,
                product,
                sku
        );

        var result = orderService.preview(
                4L,
                previewDTO()
        );

        assertEquals(
                30L,
                result.receiver().addressId()
        );

        assertEquals(1, result.items().size());
        assertEquals(2, result.totalQuantity());

        assertEquals(
                new BigDecimal("198.00"),
                result.totalAmount()
        );

        assertEquals(
                result.totalAmount(),
                result.payAmount()
        );

        var item = result.items().getFirst();

        assertEquals(100L, item.cartItemId());
        assertEquals(20L, item.productId());
        assertEquals(10L, item.skuId());
        assertEquals(2, item.quantity());

        assertEquals(
                new BigDecimal("198.00"),
                item.subtotal()
        );
    }

    @Test
    void previewShouldRejectAddressNotOwnedByMember() {

        when(addressMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.ADDRESS_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cartItemMapper,
                productMapper,
                skuStockMapper
        );
    }

    @Test
    void previewShouldRejectNoSelectedCartItems() {

        when(addressMapper.selectOne(any()))
                .thenReturn(address());

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.CART_NO_SELECTED_ITEMS,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                productMapper,
                skuStockMapper
        );
    }

    @Test
    void previewShouldRejectUnpublishedProduct() {

        PmsProduct product = product();
        product.setPublishStatus(0);

        stubPreviewData(
                address(),
                cartItem(),
                product,
                sku()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void previewShouldRejectInsufficientStock() {

        PmsSkuStock sku = sku();
        sku.setStock(2);
        sku.setLockedStock(1);

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.STOCK_INSUFFICIENT,
                exception.getErrorCode()
        );
    }

    @Test
    void previewShouldRejectSkuBelongingToAnotherProduct() {

        PmsSkuStock sku = sku();
        sku.setProductId(999L);

        stubPreviewData(
                address(),
                cartItem(),
                product(),
                sku
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.preview(
                        4L,
                        previewDTO()
                )
        );

        assertSame(
                ErrorCode.DATA_CONFLICT,
                exception.getErrorCode()
        );
    }

    private void stubPreviewData(
            UmsMemberReceiveAddress address,
            OmsCartItem cartItem,
            PmsProduct product,
            PmsSkuStock sku) {

        when(addressMapper.selectOne(any()))
                .thenReturn(address);

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of(cartItem));

        when(productMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(product));

        when(skuStockMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(sku));
    }

    private OrderPreviewDTO previewDTO() {
        OrderPreviewDTO dto = new OrderPreviewDTO();
        dto.setAddressId(30L);
        return dto;
    }

    private UmsMemberReceiveAddress address() {
        UmsMemberReceiveAddress address =
                new UmsMemberReceiveAddress();

        address.setId(30L);
        address.setMemberId(4L);
        address.setName("测试用户");
        address.setPhoneNumber("13800000000");
        address.setPostCode("100000");
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setRegion("海淀区");
        address.setDetailAddress("测试路1号");

        return address;
    }

    private OmsCartItem cartItem() {
        OmsCartItem item = new OmsCartItem();

        item.setId(100L);
        item.setMemberId(4L);
        item.setProductId(20L);
        item.setSkuId(10L);
        item.setQuantity(2);
        item.setSelected(1);

        return item;
    }

    private PmsProduct product() {
        PmsProduct product = new PmsProduct();

        product.setId(20L);
        product.setName("测试商品");
        product.setPic("product.jpg");
        product.setPublishStatus(1);

        return product;
    }

    private PmsSkuStock sku() {
        PmsSkuStock sku = new PmsSkuStock();

        sku.setId(10L);
        sku.setProductId(20L);
        sku.setSkuCode("TEST-SKU");
        sku.setPrice(new BigDecimal("99.00"));
        sku.setStock(10);
        sku.setLockedStock(0);
        sku.setSpecData("{}");

        return sku;
    }
}