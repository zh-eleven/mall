package com.mall.product.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

public class MybatisTestSupport {

    private MybatisTestSupport() {
    }

    public static void initializeTableInfo(Class<?>... entityTypes) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "test");

        for (Class<?> entityType : entityTypes) {
            TableInfoHelper.initTableInfo(assistant, entityType);
        }
    }
}
