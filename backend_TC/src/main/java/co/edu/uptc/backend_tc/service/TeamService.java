package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.PlayerSummaryDTO;
import co.edu.uptc.backend_tc.dto.response.TeamResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TeamRosterResponseDTO;
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

    // 🔹 AHORA devuelve TeamResponseDTO con datos completos
    public PageResponseDTO<TeamResponseDTO> getAll(Pageable pageable) {
        Page<Team> page = teamRepository.findAll(pageable);
        return mapperUtils.mapPage(page, this::enrichResponseDTO);
    }

    public List<TeamResponseDTO> getAllList() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream().map(this::enrichResponseDTO).toList();
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
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", dto.getTournamentId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", dto.getClubId()));

        if (teamRepository.existsByTournamentIdAndNameIgnoreCase(tournament.getId(), dto.getName())) {
            throw new ConflictException(
                    "Team with this name already exists in this tournament",
                    "name",
                    dto.getName()
            );
        }

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

        team.setIsActive(false);
        teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamRoster(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        TeamResponseDTO dto = teamMapper.toResponseDTO(team);

        // Cargar roster si existe relación
        if (team.getRoster() != null && !team.getRoster().isEmpty()) {
            dto.setRoster(
                    team.getRoster().stream()
                            .map(rosterItem -> {
                                var player = rosterItem.getPlayer();
                                return co.edu.uptc.backend_tc.dto.response.TeamRosterResponseDTO.builder()
                                        .player(co.edu.uptc.backend_tc.dto.response.PlayerSummaryDTO.builder()
                                                .id(player.getId())
                                                .fullName(player.getFullName())
                                                .documentNumber(player.getDocumentNumber())
                                                .build())
                                        .jerseyNumber(rosterItem.getJerseyNumber())
                                        .isCaptain(Boolean.TRUE.equals(rosterItem.getIsCaptain()))
                                        .build();
                            })
                            .toList()
            );
        }

        dto.setRosterSize(dto.getRoster() != null ? dto.getRoster().size() : 0);

        return dto;
    }


    private TeamResponseDTO enrichResponseDTO(Team team) {
        TeamResponseDTO dto = teamMapper.toResponseDTO(team);

        // ✅ Agregar roster completo
        dto.setRoster(
                team.getRoster().stream().map(tr ->
                        TeamRosterResponseDTO.builder()
                                .player(PlayerSummaryDTO.builder()
                                        .id(tr.getPlayer().getId())
                                        .fullName(tr.getPlayer().getFullName())
                                        .documentNumber(tr.getPlayer().getDocumentNumber())
                                        .build())
                                .jerseyNumber(tr.getJerseyNumber())
                                .isCaptain(tr.getIsCaptain() != null ? tr.getIsCaptain() : false)
                                .build()
                ).toList()
        );


        // ✅ Estadísticas básicas
        dto.setRosterSize(team.getRoster().size());
        dto.setMatchesPlayed((int) matchRepository.countByTeamId(team.getId()));

        return dto;
    }

}
