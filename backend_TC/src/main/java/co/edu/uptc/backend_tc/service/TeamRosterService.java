package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamRoster;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import co.edu.uptc.backend_tc.repository.TeamRepository;
import co.edu.uptc.backend_tc.repository.TeamRosterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamRosterService {

    private final TeamRosterRepository repository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamRosterService(TeamRosterRepository repository,
                             TeamRepository teamRepository,
                             PlayerRepository playerRepository) {
        this.repository = repository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    public List<TeamRoster> getAll() {
        return repository.findAll();
    }

    public TeamRoster create(TeamRoster teamRoster) {
        return repository.save(teamRoster);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }
}
