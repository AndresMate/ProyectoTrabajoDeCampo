package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TeamRosterDTO;
import co.edu.uptc.backend_tc.dto.response.TeamRosterResponseDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamRoster;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.TeamRosterMapper;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import co.edu.uptc.backend_tc.repository.TeamRepository;
import co.edu.uptc.backend_tc.repository.TeamRosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRosterService {

    private final TeamRosterRepository teamRosterRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamRosterMapper teamRosterMapper;

    public List<TeamRosterResponseDTO> getByTeam(Long teamId) {
        return teamRosterRepository.findByTeamIdWithPlayer(teamId)
                .stream()
                .map(teamRosterMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TeamRosterDTO> getByPlayer(Long playerId) {
        return teamRosterRepository.findByPlayerId(playerId)
                .stream()
                .map(teamRosterMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamRosterDTO addPlayerToTeam(TeamRosterDTO dto) {
        // Verificar equipo
        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", dto.getTeamId()));

        // Verificar jugador
        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", dto.getPlayerId()));

        // Verificar que no esté ya en el equipo
        if (teamRosterRepository.existsByTeamIdAndPlayerId(team.getId(), player.getId())) {
            throw new ConflictException(
                    "Player is already in this team",
                    "playerId",
                    player.getId()
            );
        }

        // Verificar número de camiseta único
        if (dto.getJerseyNumber() != null &&
                teamRosterRepository.existsByTeamIdAndJerseyNumber(team.getId(), dto.getJerseyNumber())) {
            throw new ConflictException(
                    "Jersey number already taken in this team",
                    "jerseyNumber",
                    dto.getJerseyNumber()
            );
        }

        // Verificar límite de jugadores (opcional)
        long currentPlayers = teamRosterRepository.countByTeamId(team.getId());
        if (currentPlayers >= 25) { // Límite configurable
            throw new BusinessException(
                    "Team has reached maximum number of players",
                    "MAX_PLAYERS_REACHED"
            );
        }

        TeamRoster teamRoster = TeamRoster.builder()
                .team(team)
                .player(player)
                .jerseyNumber(dto.getJerseyNumber())
                .isCaptain(dto.getIsCaptain() != null ? dto.getIsCaptain() : false)
                .build();

        teamRoster = teamRosterRepository.save(teamRoster);
        return teamRosterMapper.toDTO(teamRoster);
    }

    @Transactional
    public void removePlayerFromTeam(Long teamId, Long playerId) {
        if (!teamRosterRepository.existsByTeamIdAndPlayerId(teamId, playerId)) {
            throw new ResourceNotFoundException(
                    "Player not found in this team"
            );
        }

        teamRosterRepository.deleteByTeamIdAndPlayerId(teamId, playerId);
    }

    @Transactional
    public TeamRosterDTO setCaptain(Long teamId, Long playerId) {
        // Remover capitanía de todos
        List<TeamRoster> currentCaptains = teamRosterRepository.findByTeamIdAndIsCaptainTrue(teamId);
        currentCaptains.forEach(tr -> tr.setIsCaptain(false));
        teamRosterRepository.saveAll(currentCaptains);

        // Buscar nuevo capitán
        TeamRoster newCaptain = teamRosterRepository.findByTeamIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found in this team"));

        newCaptain.setIsCaptain(true);
        newCaptain = teamRosterRepository.save(newCaptain);

        return teamRosterMapper.toDTO(newCaptain);
    }
}
