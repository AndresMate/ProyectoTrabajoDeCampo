package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.PlayerDTO;
import co.edu.uptc.backend_tc.dto.filter.PlayerFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.PlayerResponseDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.PlayerMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import co.edu.uptc.backend_tc.repository.TeamRosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final PlayerMapper playerMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<PlayerDTO> getAll(Pageable pageable) {
        Page<Player> page = playerRepository.findAll(pageable);
        return mapperUtils.mapPage(page, playerMapper::toDTO);
    }

    public PageResponseDTO<PlayerDTO> search(PlayerFilterDTO filter, Pageable pageable) {
        Specification<Player> spec = buildSpecification(filter);
        Page<Player> page = playerRepository.findAll(spec, pageable);
        return mapperUtils.mapPage(page, playerMapper::toDTO);
    }

    public PlayerResponseDTO getById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        PlayerResponseDTO response = playerMapper.toResponseDTO(player);

        // Agregar estadísticas
        response.setTeamsCount((int) teamRosterRepository.countByPlayerId(id));
        // Aquí agregarías más estadísticas desde otros repositorios

        return response;
    }

    @Transactional
    public PlayerDTO create(PlayerDTO dto) {
        // Validar documento único
        if (playerRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new ConflictException(
                    "Player with this document number already exists",
                    "documentNumber",
                    dto.getDocumentNumber()
            );
        }

        // Validar email único (si se proporciona)
        if (dto.getInstitutionalEmail() != null &&
                playerRepository.existsByInstitutionalEmail(dto.getInstitutionalEmail())) {
            throw new ConflictException(
                    "Player with this email already exists",
                    "institutionalEmail",
                    dto.getInstitutionalEmail()
            );
        }

        // Validar código estudiantil único (si se proporciona)
        if (dto.getStudentCode() != null &&
                playerRepository.existsByStudentCode(dto.getStudentCode())) {
            throw new ConflictException(
                    "Player with this student code already exists",
                    "studentCode",
                    dto.getStudentCode()
            );
        }

        // Validar edad mínima (regla de negocio)
        if (dto.getBirthDate() != null) {
            int age = Period.between(dto.getBirthDate(), LocalDate.now()).getYears();
            if (age < 16) {
                throw new BusinessException(
                        "Player must be at least 16 years old",
                        "AGE_REQUIREMENT_NOT_MET"
                );
            }
            if (age > 100) {
                throw new BusinessException(
                        "Invalid birth date",
                        "INVALID_BIRTH_DATE"
                );
            }
        }

        Player player = playerMapper.toEntity(dto);
        player = playerRepository.save(player);
        return playerMapper.toDTO(player);
    }

    @Transactional
    public PlayerDTO update(Long id, PlayerDTO dto) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Validar documento único si cambió
        if (!player.getDocumentNumber().equals(dto.getDocumentNumber()) &&
                playerRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new ConflictException(
                    "Player with this document number already exists",
                    "documentNumber",
                    dto.getDocumentNumber()
            );
        }

        // Validar email único si cambió
        if (dto.getInstitutionalEmail() != null &&
                !dto.getInstitutionalEmail().equals(player.getInstitutionalEmail()) &&
                playerRepository.existsByInstitutionalEmail(dto.getInstitutionalEmail())) {
            throw new ConflictException(
                    "Player with this email already exists",
                    "institutionalEmail",
                    dto.getInstitutionalEmail()
            );
        }

        playerMapper.updateEntityFromDTO(dto, player);
        player = playerRepository.save(player);
        return playerMapper.toDTO(player);
    }

    @Transactional
    public void delete(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Verificar que no esté en equipos activos
        if (teamRosterRepository.existsByPlayerIdAndTeamIsActiveTrue(id)) {
            throw new BusinessException(
                    "Cannot delete player who is in active teams",
                    "PLAYER_IN_ACTIVE_TEAM"
            );
        }

        // Soft delete
        player.setIsActive(false);
        playerRepository.save(player);
    }

    // Método helper para construir especificaciones de búsqueda
    private Specification<Player> buildSpecification(PlayerFilterDTO filter) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (filter.getFullName() != null && !filter.getFullName().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("fullName")),
                        "%" + filter.getFullName().toLowerCase() + "%"
                ));
            }

            if (filter.getDocumentNumber() != null) {
                predicates.add(cb.equal(root.get("documentNumber"), filter.getDocumentNumber()));
            }

            if (filter.getStudentCode() != null) {
                predicates.add(cb.equal(root.get("studentCode"), filter.getStudentCode()));
            }

            if (filter.getInstitutionalEmail() != null) {
                predicates.add(cb.equal(root.get("institutionalEmail"), filter.getInstitutionalEmail()));
            }

            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}