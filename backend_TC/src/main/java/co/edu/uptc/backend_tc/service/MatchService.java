package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.mapper.MatchMapper;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final ScenarioRepository scenarioRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final MatchMapper matchMapper;

    public List<MatchResponseDTO> getAllMatches() {
        return matchRepository.findAllWithRelations().stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public MatchResponseDTO getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + id));
        return matchMapper.toResponseDTO(match);
    }

    public List<MatchResponseDTO> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<MatchResponseDTO> getMatchesByTournamentAndCategory(Long tournamentId, Long categoryId) {
        List<Match> matches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        return matches.stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatchDTO createMatch(MatchDTO dto) {
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Scenario scenario = dto.getScenarioId() != null
                ? scenarioRepository.findById(dto.getScenarioId()).orElse(null)
                : null;
        Team homeTeam = teamRepository.findById(dto.getHomeTeamId())
                .orElseThrow(() -> new RuntimeException("Home team not found"));
        Team awayTeam = teamRepository.findById(dto.getAwayTeamId())
                .orElseThrow(() -> new RuntimeException("Away team not found"));
        User referee = dto.getRefereeId() != null
                ? userRepository.findById(dto.getRefereeId()).orElse(null)
                : null;

        Match match = matchMapper.toEntity(dto, tournament, category, scenario, homeTeam, awayTeam, referee);
        match = matchRepository.save(match);

        return matchMapper.toDTO(match);
    }

    @Transactional
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    @Transactional
    public MatchResponseDTO startMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new RuntimeException("Solo se pueden iniciar partidos programados");
        }

        match.setStatus(MatchStatus.IN_PROGRESS);
        match = matchRepository.save(match);
        return matchMapper.toResponseDTO(match);
    }

    @Transactional
    public MatchResponseDTO finishMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new RuntimeException("Solo se pueden finalizar partidos en curso");
        }

        match.setStatus(MatchStatus.FINISHED);
        match = matchRepository.save(match);
        return matchMapper.toResponseDTO(match);
    }

    @Transactional
    public MatchResponseDTO updateMatch(Long id, MatchDTO dto) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (dto.getHomeTeamId() != null) {
            Team homeTeam = teamRepository.findById(dto.getHomeTeamId())
                    .orElseThrow(() -> new RuntimeException("Home team not found"));
            match.setHomeTeam(homeTeam);
        }

        if (dto.getAwayTeamId() != null) {
            Team awayTeam = teamRepository.findById(dto.getAwayTeamId())
                    .orElseThrow(() -> new RuntimeException("Away team not found"));
            match.setAwayTeam(awayTeam);
        }

        if (dto.getStartsAt() != null) {
            match.setStartsAt(dto.getStartsAt());
        }

        if (dto.getScenarioId() != null) {
            Scenario scenario = scenarioRepository.findById(dto.getScenarioId())
                    .orElse(null);
            match.setScenario(scenario);
        }

        match = matchRepository.save(match);
        return matchMapper.toResponseDTO(match);
    }
}
