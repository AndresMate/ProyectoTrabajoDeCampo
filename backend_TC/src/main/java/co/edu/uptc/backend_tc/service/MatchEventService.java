package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.dto.response.MatchEventResponseDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchEvent;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.MatchEventMapper;
import co.edu.uptc.backend_tc.mapper.TeamMapper;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.repository.MatchEventRepository;
import co.edu.uptc.backend_tc.repository.MatchRepository;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import co.edu.uptc.backend_tc.repository.TeamRosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchEventService {

    private final MatchEventRepository matchEventRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final MatchEventMapper matchEventMapper;
    private final TeamMapper teamMapper;

    public List<MatchEventResponseDTO> getByMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", matchId));

        return matchEventRepository.findByMatchIdOrderByMinuteAsc(matchId)
                .stream()
                .map(event -> {
                    // Determinar el equipo del jugador
                    var team = event.getPlayer().getTeamRosters().stream()
                            .filter(tr -> tr.getTeam().getId().equals(match.getHomeTeam().getId()) ||
                                    tr.getTeam().getId().equals(match.getAwayTeam().getId()))
                            .findFirst()
                            .map(tr -> teamMapper.toSummaryDTO(tr.getTeam()))
                            .orElse(null);

                    return matchEventMapper.toResponseDTO(event, team);
                })
                .collect(Collectors.toList());
    }

    public List<MatchEventDTO> getByPlayer(Long playerId) {
        return matchEventRepository.findByPlayerId(playerId)
                .stream()
                .map(matchEventMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatchEventDTO create(MatchEventDTO dto) {
        // Verificar partido
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", dto.getMatchId()));

        // Solo se pueden agregar eventos a partidos IN_PROGRESS o FINISHED
        if (match.getStatus() == MatchStatus.CANCELLED) {
            throw new BusinessException(
                    "Cannot add events to a cancelled match",
                    "INVALID_MATCH_STATUS"
            );
        }


        // Verificar jugador
        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", dto.getPlayerId()));

        // Verificar que el jugador esté en uno de los equipos
        boolean playerInMatch = teamRosterRepository.existsByPlayerIdAndTeamId(
                player.getId(), match.getHomeTeam().getId()
        ) || teamRosterRepository.existsByPlayerIdAndTeamId(
                player.getId(), match.getAwayTeam().getId()
        );

        if (!playerInMatch) {
            throw new BadRequestException(
                    "Player is not part of any team in this match"
            );
        }

        // Validar minuto
        if (dto.getMinute() != null && (dto.getMinute() < 0 || dto.getMinute() > 200)) {
            throw new BadRequestException("Match minute must be between 0 and 200");
        }

        MatchEvent event = matchEventMapper.toEntity(dto, match, player);
        event = matchEventRepository.save(event);
        return matchEventMapper.toDTO(event);
    }

    @Transactional
    public void delete(Long id) {
        if (!matchEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Match event", "id", id);
        }
        matchEventRepository.deleteById(id);
    }
}