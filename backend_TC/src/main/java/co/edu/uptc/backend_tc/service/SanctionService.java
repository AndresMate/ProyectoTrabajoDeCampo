package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.mapper.SanctionMapper;
import co.edu.uptc.backend_tc.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SanctionService {

    private final SanctionRepository sanctionRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public SanctionService(SanctionRepository sanctionRepository,
                           PlayerRepository playerRepository,
                           TeamRepository teamRepository,
                           MatchRepository matchRepository) {
        this.sanctionRepository = sanctionRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public List<SanctionDTO> getSanctionsByTeam(Long teamId) {
        return sanctionRepository.findByTeamId(teamId).stream()
                .map(SanctionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<SanctionDTO> getSanctionsByPlayer(Long playerId) {
        return sanctionRepository.findByPlayerId(playerId).stream()
                .map(SanctionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<SanctionDTO> getSanctionsByMatch(Long matchId) {
        return sanctionRepository.findByMatchId(matchId).stream()
                .map(SanctionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public SanctionDTO addSanction(SanctionDTO dto) {
        Player player = dto.getPlayerId() != null
                ? playerRepository.findById(dto.getPlayerId()).orElse(null)
                : null;

        Team team = dto.getTeamId() != null
                ? teamRepository.findById(dto.getTeamId()).orElse(null)
                : null;

        Match match = dto.getMatchId() != null
                ? matchRepository.findById(dto.getMatchId()).orElse(null)
                : null;

        Sanction sanction = SanctionMapper.toEntity(dto, player, team, match);
        return SanctionMapper.toDTO(sanctionRepository.save(sanction));
    }

    public void deleteSanction(Long id) {
        sanctionRepository.deleteById(id);
    }
}
