package com.example.securitewebback.utils.pagination;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PaginationMapper {

    public static <E, D> Page<D> paginate(
            Pageable pageable, 
            Supplier<Page<E>> fetchFunction, 
            Function<E, D> dtoMapper) {
        
        Page<E> entityPage = fetchFunction.get();
        
        return entityPage.map(dtoMapper);
    }
}
