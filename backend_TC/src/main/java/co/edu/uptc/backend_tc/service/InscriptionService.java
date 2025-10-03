package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.repository.CategoryRepository;
import co.edu.uptc.backend_tc.repository.InscriptionRepository;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import co.edu.uptc.backend_tc.repository.TournamentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final PlayerRepository playerRepository;

    public InscriptionService(InscriptionRepository inscriptionRepository,
                              TournamentRepository tournamentRepository,
                              CategoryRepository categoryRepository,
                              PlayerRepository playerRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.playerRepository = playerRepository;
    }

    public List<Inscription> getAll() {
        return inscriptionRepository.findAll();
    }

    public Inscription getById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription not found with id: " + id));
    }

    public Inscription create(Inscription inscription) {
        return inscriptionRepository.save(inscription);
    }

    public Inscription update(Long id, Inscription inscription) {
        Inscription existing = getById(id);
        existing.setTeamName(inscription.getTeamName());
        existing.setDelegatePhone(inscription.getDelegatePhone());
        existing.setStatus(inscription.getStatus());
        existing.setTournament(inscription.getTournament());
        existing.setCategory(inscription.getCategory());
        existing.setDelegate(inscription.getDelegate());
        return inscriptionRepository.save(existing);
    }

    public void delete(Long id) {
        inscriptionRepository.deleteById(id);
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }
}
