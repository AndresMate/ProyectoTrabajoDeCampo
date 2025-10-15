package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscriptionStatusDTO {

    private Long id;
    private String teamName;
    private InscriptionStatus status;
    private String tournamentName;
    private String categoryName;
    private String delegateName;
    private String delegateEmail;
    private String delegatePhone;
    private OffsetDateTime createdAt;
    private OffsetDateTime statusUpdatedAt;
    private String statusReason;
    private Integer playerCount;

    @Builder.Default
    private boolean canBeModified = true;
}