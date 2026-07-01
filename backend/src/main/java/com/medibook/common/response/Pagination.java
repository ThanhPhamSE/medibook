package com.medibook.common.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pagination {

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

    private boolean hasNext;

    private boolean hasPrevious;
}
