package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.entity.TeamAvailability;
import co.edu.uptc.backend_tc.entity.Team;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Component
public class TeamAvailabilityMapper {

    public TeamAvailabilityDTO toDTO(TeamAvailability entity) {
        if (entity == null) return null;

        return TeamAvailabilityDTO.builder()
                .dayOfWeek(entity.getDayOfWeek() != null ? entity.getDayOfWeek().name() : null)
                .startTime(entity.getStartTime() != null ? entity.getStartTime().toString() : null)
                .endTime(entity.getEndTime() != null ? entity.getEndTime().toString() : null)
                .build();
    }

    public TeamAvailability toEntity(TeamAvailabilityDTO dto, Team team) {
        if (dto == null) return null;

        return TeamAvailability.builder()
                .team(team)
                .dayOfWeek(dto.getDayOfWeek() != null ? DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase()) : null)
                .startTime(dto.getStartTime() != null ? LocalTime.parse(dto.getStartTime()) : null)
                .endTime(dto.getEndTime() != null ? LocalTime.parse(dto.getEndTime()) : null)
                .available(true)
                .build();
    }
}
