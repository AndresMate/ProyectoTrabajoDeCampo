package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchResult;
import co.edu.uptc.backend_tc.mapper.MatchResultMapper;
import co.edu.uptc.backend_tc.repository.MatchRepository;
import co.edu.uptc.backend_tc.repository.MatchResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchResultService {

    private final MatchResultRepository matchResultRepository;
    private final MatchRepository matchRepository;
    private final StandingService standingService;
    private final SanctionService sanctionService;

    public MatchResultService(MatchResultRepository matchResultRepository,
                              MatchRepository matchRepository,
                              StandingService standingService,
                              SanctionService sanctionService) {
        this.matchResultRepository = matchResultRepository;
        this.matchRepository = matchRepository;
        this.standingService = standingService;
        this.sanctionService = sanctionService;
    }

    @Transactional
    public MatchResultDTO saveResult(MatchResultDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        MatchResult result = MatchResultMapper.toEntity(dto, match);
        result = matchResultRepository.save(result);

        // Marcar partido como finalizado
        match.setStatus(co.edu.uptc.backend_tc.model.MatchStatus.FINISHED);
        matchRepository.save(match);

        // Actualizar tabla de posiciones
        standingService.updateStandingsFromMatch(match, dto.getHomeScore(), dto.getAwayScore());

        // ⚠️ Aquí podrías revisar sanciones automáticas por eventos (ej: tarjetas rojas)
        // Ejemplo: sanctionService.autoSanction(match);

        return MatchResultMapper.toDTO(result);
    }

    public MatchResultDTO getResult(Long matchId) {
        MatchResult result = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Result not found"));
        return MatchResultMapper.toDTO(result);
    }

    public void deleteResult(Long matchId) {
        matchResultRepository.deleteById(matchId);
    }
}
