package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.PlayerDTO;
import co.edu.uptc.backend_tc.dto.response.PlayerResponseDTO;
import co.edu.uptc.backend_tc.dto.response.PlayerSummaryDTO;
import co.edu.uptc.backend_tc.entity.Player;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public PlayerDTO toDTO(Player entity) {
        if (entity == null) return null;

        return PlayerDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .studentCode(entity.getStudentCode())
                .documentNumber(entity.getDocumentNumber())
                .institutionalEmail(entity.getInstitutionalEmail())
                .birthDate(entity.getBirthDate())
                .isActive(entity.getIsActive())
                .build();
    }

    public Player toEntity(PlayerDTO dto) {
        if (dto == null) return null;

        return Player.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .studentCode(dto.getStudentCode())
                .documentNumber(dto.getDocumentNumber())
                .institutionalEmail(dto.getInstitutionalEmail())
                .birthDate(dto.getBirthDate())
                .isActive(dto.getIsActive())
                .build();
    }

    public PlayerResponseDTO toResponseDTO(Player entity) {
        if (entity == null) return null;

        return PlayerResponseDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .studentCode(entity.getStudentCode())
                .documentNumber(entity.getDocumentNumber())
                .institutionalEmail(entity.getInstitutionalEmail())
                .birthDate(entity.getBirthDate())
                .age(entity.getAge())
                .isActive(entity.getIsActive())
                // Estadísticas se agregarían desde el servicio
                .build();
    }

    public PlayerSummaryDTO toSummaryDTO(Player entity) {
        if (entity == null) return null;

        return PlayerSummaryDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .documentNumber(entity.getDocumentNumber())
                .build();
    }

    public void updateEntityFromDTO(PlayerDTO dto, Player entity) {
        if (dto.getFullName() != null) {
            entity.setFullName(dto.getFullName());
        }
        if (dto.getStudentCode() != null) {
            entity.setStudentCode(dto.getStudentCode());
        }
        if (dto.getDocumentNumber() != null) {
            entity.setDocumentNumber(dto.getDocumentNumber());
        }
        if (dto.getInstitutionalEmail() != null) {
            entity.setInstitutionalEmail(dto.getInstitutionalEmail());
        }
        if (dto.getBirthDate() != null) {
            entity.setBirthDate(dto.getBirthDate());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}