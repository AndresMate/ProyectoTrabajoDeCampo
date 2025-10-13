package co.edu.uptc.backend_tc.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldValidationError {
    private String field;
    private Object rejectedValue;
    private String message;
    private String code; // código de error específico si lo necesitas
}