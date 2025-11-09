package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchResult;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.mapper.MatchResultMapper;
import co.edu.uptc.backend_tc.repository.MatchRepository;
import co.edu.uptc.backend_tc.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import co.edu.uptc.backend_tc.exception.BadRequestException;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchResultService {

    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final StandingService standingService;
    private final MatchResultMapper matchResultMapper; // ✅ INYECTAR MAPPER

    @Transactional
    public MatchResultDTO registerOrUpdateResult(MatchResultDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", dto.getMatchId()));

        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new BadRequestException("Match already finished. Use update endpoint instead.");
        }

        // Verificar si ya existe un resultado
        MatchResult existingResult = matchResultRepository.findById(dto.getMatchId()).orElse(null);
        
        if (existingResult != null) {
            // Si ya existe, revertir estadísticas anteriores
            standingService.revertStandingsFromMatch(match, existingResult.getHomeScore(), existingResult.getAwayScore());
            
            // Actualizar resultado existente
            existingResult.setHomeScore(dto.getHomeScore());
            existingResult.setAwayScore(dto.getAwayScore());
            existingResult.setNotes(dto.getNotes());
            existingResult.setEnteredAt(LocalDateTime.now());
        } else {
            // Crear nuevo resultado
            existingResult = matchResultMapper.toEntity(dto, match);
        }

        MatchResult savedResult = matchResultRepository.save(existingResult);

        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);

        // Aplicar nuevas estadísticas
        standingService.updateStandingsFromMatch(match, dto.getHomeScore(), dto.getAwayScore());

        return matchResultMapper.toDTO(savedResult);
    }

    @Transactional
    public MatchResultDTO updateResult(MatchResultDTO dto) {
        MatchResult result = matchResultRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", "matchId", dto.getMatchId()));

        Match match = result.getMatch();
        
        // Guardar valores anteriores para revertir estadísticas
        Integer oldHomeScore = result.getHomeScore();
        Integer oldAwayScore = result.getAwayScore();
        
        // Revertir estadísticas del resultado anterior
        standingService.revertStandingsFromMatch(match, oldHomeScore, oldAwayScore);

        // Actualizar resultado
        result.setHomeScore(dto.getHomeScore());
        result.setAwayScore(dto.getAwayScore());
        result.setNotes(dto.getNotes());
        result.setEnteredAt(LocalDateTime.now());

        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);

        // Aplicar nuevas estadísticas
        standingService.updateStandingsFromMatch(match, dto.getHomeScore(), dto.getAwayScore());

        return matchResultMapper.toDTO(matchResultRepository.save(result));
    }

    public MatchResultDTO getResultByMatchId(Long matchId) {
        return matchResultRepository.findById(matchId)
                .map(matchResultMapper::toDTO) // ✅ USAR MAPPER INYECTADO
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", "matchId", matchId));
    }

    @Transactional
    public void deleteResult(Long matchId) {
        MatchResult result = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", "matchId", matchId));

        Match match = result.getMatch();
        
        // Revertir estadísticas antes de eliminar
        standingService.revertStandingsFromMatch(match, result.getHomeScore(), result.getAwayScore());

        match.setStatus(MatchStatus.SCHEDULED);
        matchRepository.save(match);

        matchResultRepository.delete(result);
    }
}