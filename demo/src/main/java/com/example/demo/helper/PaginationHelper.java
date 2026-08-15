package com.example.demo.helper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaginationHelper {

    public <Entity> Page<Entity> paginateList(List<Entity> list, Pageable pageable) {
        if (list == null || list.isEmpty()) {
            return Page.empty(pageable);
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        if (start >= list.size()) {
            return Page.empty(pageable);
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
}