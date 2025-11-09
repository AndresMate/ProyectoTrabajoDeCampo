package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.StandingDTO;
import co.edu.uptc.backend_tc.dto.response.StandingResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.*;
import co.edu.uptc.backend_tc.mapper.StandingMapper;
import co.edu.uptc.backend_tc.repository.StandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// --- AÑADIR IMPORTS PARA LOGGING ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StandingService {

    private final StandingRepository standingRepository;
    private final StandingMapper standingMapper;

    private static final Logger log = LoggerFactory.getLogger(StandingService.class);

    public List<StandingDTO> getStandings(Long tournamentId, Long categoryId) {
        return standingRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId)
                .stream()
                .map(standingMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<StandingResponseDTO> getStandingsWithPosition(Long tournamentId, Long categoryId) {
        List<Standing> standings = standingRepository
                .findByTournamentIdAndCategoryId(tournamentId, categoryId);

        return IntStream.range(0, standings.size())
                .mapToObj(i -> standingMapper.toResponseDTO(
                        standings.get(i),
                        i + 1, // posición
                        null   // form - se puede calcular después
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStandingsFromMatch(Match match, Integer homeScore, Integer awayScore) {
        Tournament tournament = match.getTournament();
        Category category = match.getCategory();

        // --- AÑADIR VARIABLES DE EQUIPO ---
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();

        // --- INICIO DE LA CORRECCIÓN: VERIFICACIÓN DE NULOS ---
        // Esto previene un NullPointerException que causaría un rollback silencioso.
        if (tournament == null || category == null || homeTeam == null || awayTeam == null) {
            // Registra el error exacto en tu consola de backend
            log.error("Error crítico al actualizar standings: El partido con ID {} está incompleto.", match.getId());
            log.error("Torneo: {}, Categoría: {}, Equipo Local: {}, Equipo Visitante: {}",
                tournament, category, homeTeam, awayTeam);
            
            // Lanza una excepción clara. El @Transactional hará rollback, pero ahora
            // el Juez/Admin verá un error 500 con un mensaje útil.
            throw new IllegalStateException("Datos corruptos: El partido con ID " + match.getId() + 
                " no tiene asignado Torneo, Categoría o Equipos. No se puede guardar el resultado.");
        }
        // --- FIN DE LA CORRECCIÓN ---
        // Standing del equipo local
        Standing home = standingRepository.findByTournamentIdAndCategoryIdAndTeamId(
                tournament.getId(), category.getId(), match.getHomeTeam().getId()
        ).orElseGet(() -> initStanding(tournament, category, match.getHomeTeam()));

        // Standing del equipo visitante
        Standing away = standingRepository.findByTournamentIdAndCategoryIdAndTeamId(
                tournament.getId(), category.getId(), match.getAwayTeam().getId()
        ).orElseGet(() -> initStanding(tournament, category, match.getAwayTeam()));

        // Actualizar estadísticas
        home.setPlayed(home.getPlayed() + 1);
        away.setPlayed(away.getPlayed() + 1);

        home.setGoalsFor(home.getGoalsFor() + homeScore);
        home.setGoalsAgainst(home.getGoalsAgainst() + awayScore);
        away.setGoalsFor(away.getGoalsFor() + awayScore);
        away.setGoalsAgainst(away.getGoalsAgainst() + homeScore);

        if (homeScore > awayScore) {
            home.setWins(home.getWins() + 1);
            home.setPoints(home.getPoints() + 3);
            away.setLosses(away.getLosses() + 1);
        } else if (homeScore < awayScore) {
            away.setWins(away.getWins() + 1);
            away.setPoints(away.getPoints() + 3);
            home.setLosses(home.getLosses() + 1);
        } else {
            home.setDraws(home.getDraws() + 1);
            away.setDraws(away.getDraws() + 1);
            home.setPoints(home.getPoints() + 1);
            away.setPoints(away.getPoints() + 1);
        }

        standingRepository.save(home);
        standingRepository.save(away);
    }

    private Standing initStanding(Tournament tournament, Category category, Team team) {
        Standing standing = Standing.builder()
                .tournament(tournament)
                .category(category)
                .team(team)
                .points(0)
                .played(0)
                .wins(0)
                .draws(0)
                .losses(0)
                .goalsFor(0)
                .goalsAgainst(0)
                .build();
        return standingRepository.save(standing);
    }

    @Transactional
    public void revertStandingsFromMatch(Match match, Integer homeScore, Integer awayScore) {
        Tournament tournament = match.getTournament();
        Category category = match.getCategory();

        // Obtener standings existentes
        Standing home = standingRepository.findByTournamentIdAndCategoryIdAndTeamId(
                tournament.getId(), category.getId(), match.getHomeTeam().getId()
        ).orElse(null);

        Standing away = standingRepository.findByTournamentIdAndCategoryIdAndTeamId(
                tournament.getId(), category.getId(), match.getAwayTeam().getId()
        ).orElse(null);

        // Si no existen standings, no hay nada que revertir
        if (home == null || away == null) {
            return;
        }

        // Revertir estadísticas (restar en lugar de sumar)
        home.setPlayed(Math.max(0, home.getPlayed() - 1));
        away.setPlayed(Math.max(0, away.getPlayed() - 1));

        home.setGoalsFor(Math.max(0, home.getGoalsFor() - homeScore));
        home.setGoalsAgainst(Math.max(0, home.getGoalsAgainst() - awayScore));
        away.setGoalsFor(Math.max(0, away.getGoalsFor() - awayScore));
        away.setGoalsAgainst(Math.max(0, away.getGoalsAgainst() - homeScore));

        // Revertir resultado del partido
        if (homeScore > awayScore) {
            // Era victoria local, revertir
            home.setWins(Math.max(0, home.getWins() - 1));
            home.setPoints(Math.max(0, home.getPoints() - 3));
            away.setLosses(Math.max(0, away.getLosses() - 1));
        } else if (homeScore < awayScore) {
            // Era victoria visitante, revertir
            away.setWins(Math.max(0, away.getWins() - 1));
            away.setPoints(Math.max(0, away.getPoints() - 3));
            home.setLosses(Math.max(0, home.getLosses() - 1));
        } else {
            // Era empate, revertir
            home.setDraws(Math.max(0, home.getDraws() - 1));
            away.setDraws(Math.max(0, away.getDraws() - 1));
            home.setPoints(Math.max(0, home.getPoints() - 1));
            away.setPoints(Math.max(0, away.getPoints() - 1));
        }

        standingRepository.save(home);
        standingRepository.save(away);
    }

    @Transactional
    public void recalculateStandings(Long tournamentId, Long categoryId, List<MatchResult> results) {
        // Borrar standings previos
        standingRepository.deleteByTournamentIdAndCategoryId(tournamentId, categoryId);

        // Reprocesar todos los resultados
        for (MatchResult result : results) {
            updateStandingsFromMatch(
                    result.getMatch(),
                    result.getHomeScore(),
                    result.getAwayScore()
            );
        }
    }

}