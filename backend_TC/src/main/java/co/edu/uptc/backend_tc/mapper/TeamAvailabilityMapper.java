package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.entity.TeamAvailability;
import co.edu.uptc.backend_tc.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamAvailabilityMapper {

    public TeamAvailabilityDTO toDTO(TeamAvailability entity) {
        if (entity == null) return null;

        return TeamAvailabilityDTO.builder()
                .id(entity.getId())
                .teamId(entity.getTeam() != null ? entity.getTeam().getId() : null)
                .dayOfWeek(entity.getDayOfWeek())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .available(entity.getAvailable())
                .build();
    }

    public TeamAvailability toEntity(TeamAvailabilityDTO dto, Team team) {
        if (dto == null) return null;

        return TeamAvailability.builder()
                .id(dto.getId())
                .team(team)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .available(dto.getAvailable())
                .build();
    }
}