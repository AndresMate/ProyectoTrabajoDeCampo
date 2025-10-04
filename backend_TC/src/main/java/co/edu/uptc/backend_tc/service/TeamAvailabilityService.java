package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamAvailability;
import co.edu.uptc.backend_tc.mapper.TeamAvailabilityMapper;
import co.edu.uptc.backend_tc.repository.TeamAvailabilityRepository;
import co.edu.uptc.backend_tc.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamAvailabilityService {

    private final TeamAvailabilityRepository availabilityRepository;
    private final TeamRepository teamRepository;

    public TeamAvailabilityService(TeamAvailabilityRepository availabilityRepository, TeamRepository teamRepository) {
        this.availabilityRepository = availabilityRepository;
        this.teamRepository = teamRepository;
    }

    public List<TeamAvailabilityDTO> getByTeam(Long teamId) {
        return availabilityRepository.findByTeamId(teamId)
                .stream()
                .map(TeamAvailabilityMapper::toDTO)
                .toList();
    }

    public List<TeamAvailabilityDTO> saveAvailabilities(Long teamId, List<TeamAvailabilityDTO> dtoList, boolean isNocturno) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Validación: al menos un horario por día de lunes a viernes
        Map<DayOfWeek, List<TeamAvailabilityDTO>> byDay = dtoList.stream()
                .collect(Collectors.groupingBy(TeamAvailabilityDTO::getDayOfWeek));

        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            if (!byDay.containsKey(day)) {
                throw new RuntimeException("Each weekday must have at least one availability slot.");
            }
        }

        // Validación: rango de horario según torneo
        LocalTime min = isNocturno ? LocalTime.of(17, 0) : LocalTime.of(11, 0);
        LocalTime max = isNocturno ? LocalTime.of(21, 0) : LocalTime.of(16, 0);

        for (TeamAvailabilityDTO dto : dtoList) {
            if (dto.getStartTime().isBefore(min) || dto.getEndTime().isAfter(max)) {
                throw new RuntimeException("Invalid time range for tournament type");
            }
        }

        // Validación: no permitir solapamientos
        for (DayOfWeek day : byDay.keySet()) {
            List<TeamAvailabilityDTO> slots = byDay.get(day);
            slots.sort(Comparator.comparing(TeamAvailabilityDTO::getStartTime));
            for (int i = 0; i < slots.size() - 1; i++) {
                if (slots.get(i).getEndTime().isAfter(slots.get(i + 1).getStartTime())) {
                    throw new RuntimeException("Overlapping time slots for " + day);
                }
            }
        }

        // Guardar
        List<TeamAvailability> entities = dtoList.stream()
                .map(dto -> TeamAvailabilityMapper.toEntity(dto, team))
                .toList();

        return availabilityRepository.saveAll(entities)
                .stream()
                .map(TeamAvailabilityMapper::toDTO)
                .toList();
    }
}
