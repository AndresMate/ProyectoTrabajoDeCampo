package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.entity.User;

public class TournamentMapper {

    public static TournamentDTO toDTO(Tournament t) {
        return TournamentDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .maxTeams(t.getMaxTeams())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .modality(t.getModality())
                .status(t.getStatus())
                .sportId(t.getSport().getId())
                .createdById(t.getCreatedBy().getId())
                .build();
    }

    public static Tournament toEntity(TournamentDTO dto, Sport sport, User createdBy) {
        return Tournament.builder()
                .id(dto.getId())
                .name(dto.getName())
                .maxTeams(dto.getMaxTeams())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .modality(dto.getModality())
                .status(dto.getStatus())
                .sport(sport)
                .createdBy(createdBy)
                .build();
    }
}
