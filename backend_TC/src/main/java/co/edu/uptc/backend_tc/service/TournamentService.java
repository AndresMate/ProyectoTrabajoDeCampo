package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.repository.TournamentRepository;
import co.edu.uptc.backend_tc.repository.SportRepository;
import co.edu.uptc.backend_tc.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             SportRepository sportRepository,
                             UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.sportRepository = sportRepository;
        this.userRepository = userRepository;
    }

    public List<Tournament> getAll() {
        return tournamentRepository.findAll();
    }

    public Tournament getById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));
    }

    public Tournament create(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public Tournament update(Long id, Tournament tournament) {
        Tournament existing = getById(id);
        existing.setName(tournament.getName());
        existing.setMaxTeams(tournament.getMaxTeams());
        existing.setStartDate(tournament.getStartDate());
        existing.setEndDate(tournament.getEndDate());
        existing.setModality(tournament.getModality());
        existing.setStatus(tournament.getStatus());
        existing.setSport(tournament.getSport());
        existing.setCreatedBy(tournament.getCreatedBy());
        return tournamentRepository.save(existing);
    }

    public void delete(Long id) {
        tournamentRepository.deleteById(id);
    }

    public Sport getSportById(Long id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sport not found with id: " + id));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}
