package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.MatchEvent;
import co.edu.uptc.backend_tc.mapper.MatchEventMapper;
import co.edu.uptc.backend_tc.repository.MatchEventRepository;
import co.edu.uptc.backend_tc.repository.MatchRepository;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchEventService {

    private final MatchEventRepository matchEventRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public MatchEventService(MatchEventRepository matchEventRepository,
                             MatchRepository matchRepository,
                             PlayerRepository playerRepository) {
        this.matchEventRepository = matchEventRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    public MatchEventDTO create(MatchEventDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        MatchEvent event = MatchEventMapper.toEntity(dto, match, player);
        return MatchEventMapper.toDTO(matchEventRepository.save(event));
    }

    public List<MatchEventDTO> getByMatch(Long matchId) {
        return matchEventRepository.findByMatchId(matchId)
                .stream()
                .map(MatchEventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchEventDTO> getByPlayer(Long playerId) {
        return matchEventRepository.findByPlayerId(playerId)
                .stream()
                .map(MatchEventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        matchEventRepository.deleteById(id);
    }
}
