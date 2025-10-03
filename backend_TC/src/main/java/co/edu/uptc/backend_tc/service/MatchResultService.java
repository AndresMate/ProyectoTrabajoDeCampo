package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.mapper.MatchResultMapper;
import co.edu.uptc.backend_tc.repository.*;
import org.springframework.stereotype.Service;

@Service
public class MatchResultService {

    private final MatchResultRepository matchResultRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final StandingService standingService;

    public MatchResultService(MatchResultRepository matchResultRepository,
                              MatchRepository matchRepository,
                              UserRepository userRepository,
                              StandingService standingService) {
        this.matchResultRepository = matchResultRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.standingService = standingService;
    }

    public MatchResultDTO getByMatchId(Long matchId) {
        return matchResultRepository.findById(matchId)
                .map(MatchResultMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Result not found for match " + matchId));
    }

    public MatchResultDTO createOrUpdateResult(MatchResultDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        User enteredBy = userRepository.findById(dto.getEnteredById())
                .orElseThrow(() -> new RuntimeException("EnteredBy user not found"));
        User validatedBy = dto.getValidatedById() != null
                ? userRepository.findById(dto.getValidatedById()).orElse(null)
                : null;

        // Guardar resultado
        MatchResult matchResult = MatchResultMapper.toEntity(dto, match, enteredBy, validatedBy);
        matchResult = matchResultRepository.save(matchResult);

        // 🔥 Actualizar standings automáticamente
        standingService.updateStandingsFromMatch(match, dto.getHomeScore(), dto.getAwayScore());

        return MatchResultMapper.toDTO(matchResult);
    }

    public void deleteResult(Long matchId) {
        matchResultRepository.deleteById(matchId);
    }
}
