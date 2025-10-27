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

        // Agrupar por DayOfWeek (convertir desde el campo del DTO)
        Map<DayOfWeek, List<TeamAvailabilityDTO>> byDay = dtoList.stream()
                .collect(Collectors.groupingBy(dto -> {
                    if (dto.getDayOfWeek() == null) {
                        throw new BadRequestException("dayOfWeek is required");
                    }
                    try {
                        return DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid dayOfWeek: " + dto.getDayOfWeek());
                    }
                }));

        // Validación: al menos un horario por día de lunes a viernes
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
            LocalTime start = parseTime(dto.getStartTime());
            LocalTime end = parseTime(dto.getEndTime());

            if (start.isBefore(min) || end.isAfter(max)) {
                throw new BadRequestException(
                        String.format("Time range must be between %s and %s for this tournament type",
                                min, max)
                );
            }

            if (!end.isAfter(start) && !end.equals(start)) {
                throw new BadRequestException("endTime must be after startTime");
            }
        }

        // Validación: no permitir solapamientos por día
        for (DayOfWeek day : byDay.keySet()) {
            List<TeamAvailabilityDTO> slots = byDay.get(day);
            slots.sort(Comparator.comparing(s -> parseTime(s.getStartTime())));

            for (int i = 0; i < slots.size() - 1; i++) {
                LocalTime endCurrent = parseTime(slots.get(i).getEndTime());
                LocalTime startNext = parseTime(slots.get(i + 1).getStartTime());
                if (endCurrent.isAfter(startNext)) {
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

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null) {
            throw new BadRequestException("Time value is required");
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            throw new BadRequestException("Invalid time format: " + timeStr + " (expected HH:mm[:ss])");
        }
    }
}
