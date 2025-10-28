package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.mapper.MatchMapper;
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

    // ✅ Devuelve partidos con información completa
    public List<MatchResponseDTO> getAllMatches() {
        return matchRepository.findAllWithRelations().stream()
                .map(matchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ Devuelve un partido con relaciones anidadas
    public MatchResponseDTO getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + id));
        return matchMapper.toResponseDTO(match);
    }

    // ✅ Crea un partido nuevo (recibe DTO con IDs)
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
}
