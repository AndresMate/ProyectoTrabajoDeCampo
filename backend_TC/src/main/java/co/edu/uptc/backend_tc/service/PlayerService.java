package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    public List<Player> getAll() {
        return repository.findAll();
    }

    public Player getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public Player create(Player player) {
        return repository.save(player);
    }

    public Player update(Long id, Player player) {
        Player existing = getById(id);
        existing.setFullName(player.getFullName());
        existing.setStudentCode(player.getStudentCode());
        existing.setDocumentNumber(player.getDocumentNumber());
        existing.setInstitutionalEmail(player.getInstitutionalEmail());
        existing.setBirthDate(player.getBirthDate());
        existing.setIsActive(player.getIsActive());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
