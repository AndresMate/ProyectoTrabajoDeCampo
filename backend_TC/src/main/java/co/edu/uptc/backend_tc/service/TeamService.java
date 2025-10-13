package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TeamResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.TeamMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ClubRepository clubRepository;
    private final MatchRepository matchRepository;
    private final TeamMapper teamMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<TeamDTO> getAll(Pageable pageable) {
        Page<Team> page = teamRepository.findAll(pageable);
        return mapperUtils.mapPage(page, teamMapper::toDTO);
    }

    public List<TeamDTO> getByTournament(Long tournamentId) {
        return mapperUtils.mapList(
                teamRepository.findByTournamentId(tournamentId),
                teamMapper::toDTO
        );
    }

    public List<TeamDTO> getByTournamentAndCategory(Long tournamentId, Long categoryId) {
        return mapperUtils.mapList(
                teamRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId),
                teamMapper::toDTO
        );
    }

    public TeamResponseDTO getById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return enrichResponseDTO(team);
    }

    @Transactional
    public TeamDTO create(TeamDTO dto) {
        // Verificar torneo
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", dto.getTournamentId()));

        // Verificar categoría
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        // Verificar club
        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", dto.getClubId()));

        // Verificar nombre único en el torneo
        if (teamRepository.existsByTournamentIdAndNameIgnoreCase(tournament.getId(), dto.getName())) {
            throw new ConflictException(
                    "Team with this name already exists in this tournament",
                    "name",
                    dto.getName()
            );
        }

        // Verificar inscripción si se proporciona
        Inscription inscription = null;
        if (dto.getOriginInscriptionId() != null) {
            inscription = inscriptionRepository.findById(dto.getOriginInscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", dto.getOriginInscriptionId()));
        }

        Team team = Team.builder()
                .name(dto.getName())
                .tournament(tournament)
                .category(category)
                .club(club)
                .originInscription(inscription)
                .isActive(true)
                .build();

        team = teamRepository.save(team);
        return teamMapper.toDTO(team);
    }

    @Transactional
    public TeamDTO update(Long id, TeamDTO dto) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        // Validar nombre único si cambió
        if (!team.getName().equalsIgnoreCase(dto.getName()) &&
                teamRepository.existsByTournamentIdAndNameIgnoreCase(team.getTournament().getId(), dto.getName())) {
            throw new ConflictException(
                    "Team with this name already exists in this tournament",
                    "name",
                    dto.getName()
            );
        }

        team.setName(dto.getName());
        if (dto.getIsActive() != null) {
            team.setIsActive(dto.getIsActive());
        }

        team = teamRepository.save(team);
        return teamMapper.toDTO(team);
    }

    @Transactional
    public void delete(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        // Verificar que no tenga partidos jugados
        long playedMatches = matchRepository.countByTeamIdAndStatusNot(
                id,
                co.edu.uptc.backend_tc.model.MatchStatus.SCHEDULED
        );

        if (playedMatches > 0) {
            throw new BusinessException(
                    "Cannot delete team with played matches",
                    "TEAM_HAS_PLAYED_MATCHES"
            );
        }

        // Soft delete
        team.setIsActive(false);
        teamRepository.save(team);
    }

    private TeamResponseDTO enrichResponseDTO(Team team) {
        TeamResponseDTO dto = teamMapper.toResponseDTO(team);

        // Agregar estadísticas
        dto.setRosterSize(team.getRoster().size());
        dto.setMatchesPlayed((int) matchRepository.countByTeamId(team.getId()));

        return dto;
    }
}