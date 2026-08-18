package com.mall.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
        long pageNum,
        long pageSize,
        long total,
        long totalPages,
        List<T> list
) {

    public static <S, T> PageResult<T> from(
            IPage<S> page,
            Function<S, T> converter) {

        return new PageResult<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                page.getRecords()
                        .stream()
                        .map(converter)
                        .toList()
        );
    }
}