package co.edu.uptc.backend_tc.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MapperUtils {

    public <E, D> List<D> mapList(List<E> entities, Function<E, D> mapper) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    public <E, D> PageResponseDTO<D> mapPage(Page<E> page, Function<E, D> mapper) {
        List<D> content = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PageResponseDTO.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
