package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.InscriptionPlayer;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.repository.InscriptionPlayerRepository;
import co.edu.uptc.backend_tc.repository.InscriptionRepository;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InscriptionPlayerService {

    private final InscriptionPlayerRepository repository;
    private final InscriptionRepository inscriptionRepository;
    private final PlayerRepository playerRepository;

    public InscriptionPlayerService(InscriptionPlayerRepository repository,
                                    InscriptionRepository inscriptionRepository,
                                    PlayerRepository playerRepository) {
        this.repository = repository;
        this.inscriptionRepository = inscriptionRepository;
        this.playerRepository = playerRepository;
    }

    public List<InscriptionPlayer> getAll() {
        return repository.findAll();
    }

    public InscriptionPlayer getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InscriptionPlayer not found with id: " + id));
    }

    public InscriptionPlayer create(InscriptionPlayer inscriptionPlayer) {
        return repository.save(inscriptionPlayer);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Inscription getInscriptionById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription not found with id: " + id));
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }
}
