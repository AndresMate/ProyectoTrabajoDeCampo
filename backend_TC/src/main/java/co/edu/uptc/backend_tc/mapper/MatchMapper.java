package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResponseDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResultSummaryDTO;
import co.edu.uptc.backend_tc.dto.response.MatchSummaryDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.MatchStatus;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    private final TournamentMapper tournamentMapper;
    private final CategoryMapper categoryMapper;
    private final TeamMapper teamMapper;
    private final ScenarioMapper scenarioMapper;
    private final UserMapper userMapper;

    public MatchMapper(TournamentMapper tournamentMapper,
                       CategoryMapper categoryMapper,
                       TeamMapper teamMapper,
                       ScenarioMapper scenarioMapper,
                       UserMapper userMapper) {
        this.tournamentMapper = tournamentMapper;
        this.categoryMapper = categoryMapper;
        this.teamMapper = teamMapper;
        this.scenarioMapper = scenarioMapper;
        this.userMapper = userMapper;
    }

    public MatchDTO toDTO(Match entity) {
        if (entity == null) return null;

        return MatchDTO.builder()
                .id(entity.getId())
                .tournamentId(entity.getTournament() != null ? entity.getTournament().getId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .scenarioId(entity.getScenario() != null ? entity.getScenario().getId() : null)
                .startsAt(entity.getStartsAt())
                .homeTeamId(entity.getHomeTeam() != null ? entity.getHomeTeam().getId() : null)
                .awayTeamId(entity.getAwayTeam() != null ? entity.getAwayTeam().getId() : null)
                .status(entity.getStatus())
                .refereeId(entity.getReferee() != null ? entity.getReferee().getId() : null)
                .build();
    }

    public MatchResponseDTO toResponseDTO(Match entity) {
        if (entity == null) return null;

        MatchResultSummaryDTO resultSummary = null;
        if (entity.getResult() != null) {
            resultSummary = MatchResultSummaryDTO.builder()
                    .homeScore(entity.getResult().getHomeScore())
                    .awayScore(entity.getResult().getAwayScore())
                    .notes(entity.getResult().getNotes())
                    .build();
        }

        return MatchResponseDTO.builder()
                .id(entity.getId())
                .startsAt(entity.getStartsAt())
                .status(entity.getStatus())
                .tournament(tournamentMapper.toSummaryDTO(entity.getTournament()))
                .category(categoryMapper.toSummaryDTO(entity.getCategory()))
                .scenario(scenarioMapper.toSummaryDTO(entity.getScenario()))
                .homeTeam(teamMapper.toSummaryDTO(entity.getHomeTeam()))
                .awayTeam(teamMapper.toSummaryDTO(entity.getAwayTeam()))
                .referee(userMapper.toSummaryDTO(entity.getReferee()))
                .result(resultSummary)
                .build();
    }

    public MatchSummaryDTO toSummaryDTO(Match entity) {
        if (entity == null) return null;

        return MatchSummaryDTO.builder()
                .id(entity.getId())
                .startsAt(entity.getStartsAt())
                .status(entity.getStatus())
                .homeTeam(teamMapper.toSummaryDTO(entity.getHomeTeam()))
                .awayTeam(teamMapper.toSummaryDTO(entity.getAwayTeam()))
                .build();
    }

    public Match toEntity(MatchDTO dto, Tournament tournament, Category category, Scenario scenario, Team homeTeam, Team awayTeam, User referee) {
        Match match = new Match();

        match.setTournament(tournament);
        match.setCategory(category);
        match.setScenario(scenario);
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setReferee(referee);

        // 🔹 Agregar estos campos que estaban faltando
        match.setStartsAt(dto.getStartsAt());
        match.setStatus(dto.getStatus() != null ? dto.getStatus() : MatchStatus.SCHEDULED);

        return match;
    }


}
