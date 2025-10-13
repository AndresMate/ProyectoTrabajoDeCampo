package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.mapper.MatchMapper;
import co.edu.uptc.backend_tc.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final ScenarioRepository scenarioRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final MatchMapper matchMapper;

    public MatchService(MatchRepository matchRepository,
                        TournamentRepository tournamentRepository,
                        CategoryRepository categoryRepository,
                        ScenarioRepository scenarioRepository,
                        TeamRepository teamRepository,
                        UserRepository userRepository,
                        MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.scenarioRepository = scenarioRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.matchMapper = matchMapper;
    }

    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MatchDTO getMatchById(Long id) {
        return matchRepository.findById(id)
                .map(matchMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + id));
    }

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
        return matchMapper.toDTO(matchRepository.save(match));
    }

    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }
}

