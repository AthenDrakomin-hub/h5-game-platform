package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.entity.Promotion;
import com.h5.mapper.PromotionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PromoService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PromoServiceTest {

    @Mock private PromotionMapper promotionMapper;
    @InjectMocks private PromoService promoService;

    private Promotion promo1;
    private Promotion promo2;

    @BeforeEach
    void setUp() {
        promo1 = new Promotion();
        promo1.setId(1L);
        promo1.setTitle("新人首充奖励");
        promo1.setCategory("newbie");
        promo1.setCategoryName("新人专享");
        promo1.setStatus(1);
        promo1.setSort(1);

        promo2 = new Promotion();
        promo2.setId(2L);
        promo2.setTitle("每日签到");
        promo2.setCategory("daily");
        promo2.setCategoryName("日常活动");
        promo2.setStatus(1);
        promo2.setSort(2);
    }

    @Nested
    @DisplayName("活动分类")
    class Categories {
        @Test
        @DisplayName("按category去重返回分类列表")
        void testGetCategories() {
            when(promotionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(promo1, promo2));

            List<Map<String, Object>> categories = promoService.getCategories();
            assertEquals(2, categories.size());
            assertEquals("newbie", categories.get(0).get("code"));
            assertEquals("新人专享", categories.get(0).get("name"));
        }

        @Test
        @DisplayName("无活动时返回空列表")
        void testEmptyCategories() {
            when(promotionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList());
            List<Map<String, Object>> categories = promoService.getCategories();
            assertTrue(categories.isEmpty());
        }
    }

    @Nested
    @DisplayName("活动列表")
    class PromotionList {
        @Test
        @DisplayName("分页查询活动列表")
        void testGetPromotionList() {
            Page<Promotion> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(promo1, promo2));
            mockPage.setTotal(2);
            when(promotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            Map<String, Object> result = promoService.getPromotionList(null, 1, 10);
            assertEquals(2L, result.get("total"));
            assertNotNull(result.get("list"));
        }

        @Test
        @DisplayName("按分类筛选活动")
        void testFilterByCategory() {
            Page<Promotion> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(promo1));
            mockPage.setTotal(1);
            when(promotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            Map<String, Object> result = promoService.getPromotionList("newbie", 1, 10);
            assertEquals(1L, result.get("total"));
        }
    }

    @Nested
    @DisplayName("活动详情")
    class PromotionDetail {
        @Test
        @DisplayName("正常获取活动详情")
        void testGetPromotionDetail() {
            when(promotionMapper.selectById(1L)).thenReturn(promo1);
            Promotion result = promoService.getPromotionDetail(1L);
            assertEquals("新人首充奖励", result.getTitle());
        }

        @Test
        @DisplayName("活动不存在抛异常")
        void testPromotionNotFound() {
            when(promotionMapper.selectById(999L)).thenReturn(null);
            assertThrows(BusinessException.class, () -> promoService.getPromotionDetail(999L));
        }

        @Test
        @DisplayName("已下架活动抛异常")
        void testDisabledPromotion() {
            promo1.setStatus(0);
            when(promotionMapper.selectById(1L)).thenReturn(promo1);
            assertThrows(BusinessException.class, () -> promoService.getPromotionDetail(1L));
        }
    }
}
