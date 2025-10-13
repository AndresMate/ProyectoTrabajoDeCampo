package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentSummaryDTO;
import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    private final SportMapper sportMapper;
    private final UserMapper userMapper;

    public TournamentMapper(SportMapper sportMapper, UserMapper userMapper) {
        this.sportMapper = sportMapper;
        this.userMapper = userMapper;
    }

    public TournamentDTO toDTO(Tournament entity) {
        if (entity == null) return null;

        return TournamentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .maxTeams(entity.getMaxTeams())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .modality(entity.getModality())
                .status(entity.getStatus())
                .sportId(entity.getSport() != null ? entity.getSport().getId() : null)
                .createdById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .build();
    }

    public TournamentResponseDTO toResponseDTO(Tournament entity) {
        if (entity == null) return null;

        return TournamentResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .maxTeams(entity.getMaxTeams())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .modality(entity.getModality())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .sport(sportMapper.toSummaryDTO(entity.getSport()))
                .createdBy(userMapper.toSummaryDTO(entity.getCreatedBy()))
                // Las estadísticas se agregan desde el servicio
                .build();
    }

    public TournamentSummaryDTO toSummaryDTO(Tournament entity) {
        if (entity == null) return null;

        return TournamentSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }

    public void updateEntityFromDTO(TournamentDTO dto, Tournament entity, Sport sport, User creator) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getMaxTeams() != null) {
            entity.setMaxTeams(dto.getMaxTeams());
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        if (dto.getModality() != null) {
            entity.setModality(dto.getModality());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (sport != null) {
            entity.setSport(sport);
        }
        if (creator != null) {
            entity.setCreatedBy(creator);
        }
    }
}