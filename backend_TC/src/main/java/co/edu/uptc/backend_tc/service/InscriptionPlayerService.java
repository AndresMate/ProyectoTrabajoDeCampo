package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.InscriptionPlayerDTO;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.InscriptionPlayer;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.repository.InscriptionPlayerRepository;
import co.edu.uptc.backend_tc.repository.InscriptionRepository;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscriptionPlayerService {

    private final InscriptionPlayerRepository inscriptionPlayerRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PlayerRepository playerRepository;

    public List<InscriptionPlayerDTO> getByInscription(Long inscriptionId) {
        return inscriptionPlayerRepository.findByInscriptionId(inscriptionId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public InscriptionPlayerDTO addPlayerToInscription(InscriptionPlayerDTO dto) {
        // Verificar inscripción
        Inscription inscription = inscriptionRepository.findById(dto.getInscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", dto.getInscriptionId()));

        // Solo se pueden agregar jugadores a inscripciones PENDING
        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException(
                    "Can only add players to PENDING inscriptions",
                    "INVALID_INSCRIPTION_STATUS"
            );
        }

        // Verificar jugador
        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", dto.getPlayerId()));

        if (!player.getIsActive()) {
            throw new BusinessException(
                    "Cannot add inactive player to inscription",
                    "PLAYER_INACTIVE"
            );
        }

        // Verificar que no esté ya en la inscripción
        if (inscriptionPlayerRepository.existsByInscriptionIdAndPlayerId(
                inscription.getId(), player.getId())) {
            throw new ConflictException(
                    "Player is already in this inscription",
                    "playerId",
                    player.getId()
            );
        }

        // Verificar límite de jugadores (por ejemplo, máximo 25)
        long currentPlayers = inscriptionPlayerRepository.countByInscriptionId(inscription.getId());
        if (currentPlayers >= 25) {
            throw new BusinessException(
                    "Inscription has reached maximum number of players",
                    "MAX_PLAYERS_REACHED"
            );
        }

        InscriptionPlayer inscriptionPlayer = InscriptionPlayer.builder()
                .inscription(inscription)
                .player(player)
                .build();

        inscriptionPlayer = inscriptionPlayerRepository.save(inscriptionPlayer);
        return toDTO(inscriptionPlayer);
    }

    @Transactional
    public void removePlayerFromInscription(Long inscriptionId, Long playerId) {
        // Verificar que existe
        if (!inscriptionPlayerRepository.existsByInscriptionIdAndPlayerId(inscriptionId, playerId)) {
            throw new ResourceNotFoundException("Player not found in this inscription");
        }

        // Verificar estado de inscripción
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", inscriptionId));

        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException(
                    "Can only remove players from PENDING inscriptions",
                    "INVALID_INSCRIPTION_STATUS"
            );
        }

        inscriptionPlayerRepository.deleteByInscriptionIdAndPlayerId(inscriptionId, playerId);
    }

    private InscriptionPlayerDTO toDTO(InscriptionPlayer entity) {
        return InscriptionPlayerDTO.builder()
                .inscriptionId(entity.getInscription().getId())
                .playerId(entity.getPlayer().getId())
                .build();
    }
}