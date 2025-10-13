package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamAvailability;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.TeamAvailabilityMapper;
import co.edu.uptc.backend_tc.repository.TeamAvailabilityRepository;
import co.edu.uptc.backend_tc.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAvailabilityService {

    private final TeamAvailabilityRepository availabilityRepository;
    private final TeamRepository teamRepository;
    private final TeamAvailabilityMapper availabilityMapper;

    public List<TeamAvailabilityDTO> getByTeam(Long teamId) {
        return availabilityRepository.findByTeamId(teamId)
                .stream()
                .map(availabilityMapper::toDTO)
                .toList();
    }

    @Transactional
    public List<TeamAvailabilityDTO> saveAvailabilities(
            Long teamId,
            List<TeamAvailabilityDTO> dtoList,
            boolean isNocturno) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        // Validación: al menos un horario por día de lunes a viernes
        Map<DayOfWeek, List<TeamAvailabilityDTO>> byDay = dtoList.stream()
                .collect(Collectors.groupingBy(TeamAvailabilityDTO::getDayOfWeek));

        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            if (!byDay.containsKey(day)) {
                throw new BusinessException(
                        "Each weekday must have at least one availability slot",
                        "MISSING_WEEKDAY_AVAILABILITY"
                );
            }
        }

        // Validación: rango de horario según torneo
        LocalTime min = isNocturno ? LocalTime.of(17, 0) : LocalTime.of(11, 0);
        LocalTime max = isNocturno ? LocalTime.of(21, 0) : LocalTime.of(16, 0);

        for (TeamAvailabilityDTO dto : dtoList) {
            if (dto.getStartTime().isBefore(min) || dto.getEndTime().isAfter(max)) {
                throw new BadRequestException(
                        String.format("Time range must be between %s and %s for this tournament type",
                                min, max)
                );
            }
        }

        // Validación: no permitir solapamientos
        for (DayOfWeek day : byDay.keySet()) {
            List<TeamAvailabilityDTO> slots = byDay.get(day);
            slots.sort(Comparator.comparing(TeamAvailabilityDTO::getStartTime));

            for (int i = 0; i < slots.size() - 1; i++) {
                if (slots.get(i).getEndTime().isAfter(slots.get(i + 1).getStartTime())) {
                    throw new BusinessException(
                            "Overlapping time slots detected for " + day,
                            "OVERLAPPING_TIME_SLOTS"
                    );
                }
            }
        }

        // Eliminar disponibilidades anteriores
        availabilityRepository.deleteByTeamId(teamId);

        // Guardar nuevas disponibilidades
        List<TeamAvailability> entities = dtoList.stream()
                .map(dto -> availabilityMapper.toEntity(dto, team))
                .toList();

        return availabilityRepository.saveAll(entities)
                .stream()
                .map(availabilityMapper::toDTO)
                .toList();
    }
}