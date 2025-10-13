package co.edu.uptc.backend_tc.dto.page;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponseDTO<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;

    public static <T> PageResponseDTO<T> of(List<T> content, int page, int size, long totalElements) {
        return PageResponseDTO.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .build();
    }
}
