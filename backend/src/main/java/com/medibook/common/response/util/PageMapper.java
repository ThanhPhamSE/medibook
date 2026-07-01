package com.medibook.common.response.util;

import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.medibook.common.response.PageResponse;
import com.medibook.common.response.Pagination;

public class PageMapper {

    public static <T> PageResponse<T> from(Page<T> page) {

        return PageResponse.<T>builder().items(page.getContent())
                .pagination(Pagination.builder().page(page.getNumber()).size(page.getSize())
                        .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).first(page.isFirst())
                        .last(page.isLast()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious()).build())
                .build();

    }

    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {

        return PageResponse.<R>builder()
                .items(page.getContent().stream().map(mapper).toList())
                .pagination(buildPagination(page))
                .build();
    }

    private static Pagination buildPagination(Page<?> page) {

        return Pagination.builder().page(page.getNumber()).size(page.getSize()).totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages()).first(page.isFirst()).last(page.isLast()).hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious()).build();
    }
}
