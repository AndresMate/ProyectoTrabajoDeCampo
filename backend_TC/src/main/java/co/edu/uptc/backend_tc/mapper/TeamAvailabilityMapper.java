package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.entity.TeamAvailability;
import co.edu.uptc.backend_tc.entity.Team;

public class TeamAvailabilityMapper {

    public static TeamAvailabilityDTO toDTO(TeamAvailability ta) {
        return TeamAvailabilityDTO.builder()
                .id(ta.getId())
                .teamId(ta.getTeam().getId())
                .dayOfWeek(ta.getDayOfWeek())
                .startTime(ta.getStartTime())
                .endTime(ta.getEndTime())
                .available(ta.getAvailable())
                .build();
    }

    public static TeamAvailability toEntity(TeamAvailabilityDTO dto, Team team) {
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
