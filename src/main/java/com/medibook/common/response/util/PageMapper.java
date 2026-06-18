package com.medibook.common.response.util;

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

}
