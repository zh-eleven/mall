package com.mall.portal.cart.service;

import com.mall.portal.order.entity.OmsCartItem;
import com.mall.portal.order.mapper.OmsCartItemMapper;
import com.mall.portal.cart.dto.CartItemAddDTO;
import com.mall.portal.cart.service.impl.PortalCartServiceImpl;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.portal.cart.dto.CartItemQuantityUpdateDTO;
import java.math.BigDecimal;
import com.mall.portal.cart.dto.CartItemSelectedUpdateDTO;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalCartServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                OmsCartItem.class,
                PmsProduct.class,
                PmsSkuStock.class
        );
    }

    @Mock
    private OmsCartItemMapper cartItemMapper;

    @Mock
    private PmsProductMapper productMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @InjectMocks
    private PortalCartServiceImpl cartService;

    @Test
    void addShouldCreateNewCartItem() {

        CartItemAddDTO dto = new CartItemAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(2);

        PmsSkuStock sku = sku();
        PmsProduct product = product();

        when(skuStockMapper.selectById(10L))
                .thenReturn(sku);
        when(productMapper.selectOne(any()))
                .thenReturn(product);
        when(cartItemMapper.selectOne(any()))
                .thenReturn(null);

        when(cartItemMapper.insert(any(OmsCartItem.class)))
                .thenAnswer(invocation -> {
                    OmsCartItem item =
                            invocation.getArgument(0);
                    item.setId(100L);
                    return 1;
                });

        var result = cartService.add(4L, dto);

        ArgumentCaptor<OmsCartItem> captor =
                ArgumentCaptor.forClass(
                        OmsCartItem.class
                );

        verify(cartItemMapper).insert(
                captor.capture()
        );

        OmsCartItem inserted = captor.getValue();

        assertEquals(4L, inserted.getMemberId());
        assertEquals(20L, inserted.getProductId());
        assertEquals(10L, inserted.getSkuId());
        assertEquals(2, inserted.getQuantity());
        assertEquals(1, inserted.getSelected());

        assertEquals(100L, result.id());
        assertEquals(2, result.quantity());
        assertEquals(
                new BigDecimal("198.00"),
                result.subtotal()
        );
        assertTrue(result.available());
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
        sku.setPrice(new BigDecimal("99.00"));
        sku.setStock(10);
        sku.setLockedStock(0);
        sku.setSpecData("{}");
        return sku;
    }

    @Test
    void addShouldAccumulateExistingCartItem() {

        CartItemAddDTO dto = new CartItemAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(2);

        OmsCartItem existing = cartItem();
        existing.setQuantity(3);
        existing.setSelected(0);

        when(skuStockMapper.selectById(10L))
                .thenReturn(sku());
        when(productMapper.selectOne(any()))
                .thenReturn(product());
        when(cartItemMapper.selectOne(any()))
                .thenReturn(existing);
        when(cartItemMapper.update(isNull(), any()))
                .thenReturn(1);

        var result = cartService.add(4L, dto);

        assertEquals(5, result.quantity());
        assertTrue(result.selected());

        verify(cartItemMapper, never())
                .insert(any(OmsCartItem.class));
        verify(cartItemMapper)
                .update(isNull(), any());
    }

    @Test
    void addShouldRejectInsufficientStock() {

        CartItemAddDTO dto = new CartItemAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(11);

        when(skuStockMapper.selectById(10L))
                .thenReturn(sku());
        when(productMapper.selectOne(any()))
                .thenReturn(product());
        when(cartItemMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.add(4L, dto)
        );

        assertSame(
                ErrorCode.STOCK_INSUFFICIENT,
                exception.getErrorCode()
        );

        verify(cartItemMapper, never())
                .insert(any(OmsCartItem.class));
    }

    @Test
    void updateQuantityShouldRejectCartItemOwnedByAnotherMember() {

        CartItemQuantityUpdateDTO dto =
                new CartItemQuantityUpdateDTO();
        dto.setQuantity(2);

        when(cartItemMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateQuantity(
                        4L,
                        100L,
                        dto
                )
        );

        assertSame(
                ErrorCode.CART_ITEM_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                productMapper,
                skuStockMapper
        );
    }

    private OmsCartItem cartItem() {
        OmsCartItem item = new OmsCartItem();
        item.setId(100L);
        item.setMemberId(4L);
        item.setProductId(20L);
        item.setSkuId(10L);
        item.setQuantity(1);
        item.setSelected(1);
        return item;
    }

    @Test
    void listShouldReturnCurrentMemberCartItems() {

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of(cartItem()));

        when(productMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(product()));

        when(skuStockMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(sku()));

        var result = cartService.list(4L);

        assertEquals(1, result.size());

        var item = result.getFirst();

        assertEquals(100L, item.id());
        assertEquals(20L, item.productId());
        assertEquals(10L, item.skuId());
        assertEquals(1, item.quantity());
        assertTrue(item.selected());
        assertTrue(item.available());
        assertEquals(
                new BigDecimal("99.00"),
                item.subtotal()
        );
    }

    @Test
    void updateSelectedShouldChangeSelectedStatus() {

        OmsCartItem cartItem = cartItem();

        CartItemSelectedUpdateDTO dto =
                new CartItemSelectedUpdateDTO();
        dto.setSelected(false);

        when(cartItemMapper.selectOne(any()))
                .thenReturn(cartItem);

        when(cartItemMapper.update(isNull(), any()))
                .thenReturn(1);

        when(productMapper.selectById(20L))
                .thenReturn(product());

        when(skuStockMapper.selectById(10L))
                .thenReturn(sku());

        var result = cartService.updateSelected(
                4L,
                100L,
                dto
        );

        assertFalse(result.selected());
        assertEquals(0, cartItem.getSelected());

        verify(cartItemMapper)
                .update(isNull(), any());
    }

    @Test
    void deleteShouldDeleteOwnedCartItem() {

        when(cartItemMapper.delete(any()))
                .thenReturn(1);

        assertDoesNotThrow(
                () -> cartService.delete(4L, 100L)
        );

        verify(cartItemMapper).delete(any());
    }

    @Test
    void deleteShouldRejectMissingCartItem() {

        when(cartItemMapper.delete(any()))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.delete(4L, 100L)
        );

        assertSame(
                ErrorCode.CART_ITEM_NOT_FOUND,
                exception.getErrorCode()
        );
    }
}