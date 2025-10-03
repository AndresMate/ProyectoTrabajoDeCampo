package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ClubRepository clubRepository;

    public TeamService(TeamRepository teamRepository,
                       TournamentRepository tournamentRepository,
                       CategoryRepository categoryRepository,
                       InscriptionRepository inscriptionRepository,
                       ClubRepository clubRepository) {
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.clubRepository = clubRepository;
    }

    public List<Team> getAll() {
        return teamRepository.findAll();
    }

    public Team getById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
    }

    public Team create(Team team) {
        return teamRepository.save(team);
    }

    public Team update(Long id, Team team) {
        Team existing = getById(id);
        existing.setName(team.getName());
        existing.setIsActive(team.getIsActive());
        existing.setTournament(team.getTournament());
        existing.setCategory(team.getCategory());
        existing.setOriginInscription(team.getOriginInscription());
        existing.setClub(team.getClub());
        return teamRepository.save(existing);
    }

    public void delete(Long id) {
        teamRepository.deleteById(id);
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Inscription getInscriptionById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription not found with id: " + id));
    }

    public Club getClubById(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found with id: " + id));
    }
}
