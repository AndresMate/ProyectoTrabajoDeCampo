package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.dto.response.SanctionResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.SanctionMapper;
import co.edu.uptc.backend_tc.model.SanctionType;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SanctionService {

    private final SanctionRepository sanctionRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final SanctionMapper sanctionMapper;

    public List<SanctionResponseDTO> getSanctionsByTeam(Long teamId) {
        return sanctionRepository.findByTeamId(teamId)
                .stream()
                .map(sanctionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SanctionResponseDTO> getSanctionsByPlayer(Long playerId) {
        return sanctionRepository.findByPlayerId(playerId)
                .stream()
                .map(sanctionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SanctionResponseDTO> getActiveSanctionsByPlayer(Long playerId) {
        return sanctionRepository.findActiveByPlayerId(playerId, LocalDateTime.now())
                .stream()
                .map(sanctionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SanctionResponseDTO> getSanctionsByMatch(Long matchId) {
        return sanctionRepository.findByMatchId(matchId)
                .stream()
                .map(sanctionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SanctionDTO addSanction(SanctionDTO dto) {
        // Validar que al menos player o team esté presente
        if (dto.getPlayerId() == null && dto.getTeamId() == null) {
            throw new BadRequestException("Sanction must be for a player or a team");
        }

        Player player = null;
        if (dto.getPlayerId() != null) {
            player = playerRepository.findById(dto.getPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player", "id", dto.getPlayerId()));
        }

        Team team = null;
        if (dto.getTeamId() != null) {
            team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", dto.getTeamId()));
        }

        Match match = null;
        if (dto.getMatchId() != null) {
            match = matchRepository.findById(dto.getMatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match", "id", dto.getMatchId()));
        }

        Sanction sanction = Sanction.builder()
                .player(player)
                .team(team)
                .match(match)
                .type(dto.getType())
                .reason(dto.getReason())
                .validUntil(dto.getValidUntil())
                .build();

        sanction = sanctionRepository.save(sanction);
        return SanctionMapper.toDTO(sanction);
    }

    @Transactional
    public void deleteSanction(Long id) {
        if (!sanctionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sanction", "id", id);
        }
        sanctionRepository.deleteById(id);
    }
}