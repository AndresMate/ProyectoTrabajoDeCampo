package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.ClubDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.Club;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.ClubMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.ClubRepository;
import co.edu.uptc.backend_tc.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubRepository clubRepository;
    private final TeamRepository teamRepository;
    private final ClubMapper clubMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<ClubDTO> getAll(Pageable pageable) {
        Page<Club> page = clubRepository.findAll(pageable);
        return mapperUtils.mapPage(page, clubMapper::toDTO);
    }

    public List<ClubDTO> getAllActive() {
        return mapperUtils.mapList(
                clubRepository.findByIsActiveTrue(),
                clubMapper::toDTO
        );
    }

    public ClubDTO getById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", id));
        return clubMapper.toDTO(club);
    }

    @Transactional
    public ClubDTO create(ClubDTO dto) {
        // Validar nombre único
        if (clubRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException(
                    "A club with this name already exists",
                    "name",
                    dto.getName()
            );
        }

        Club club = clubMapper.toEntity(dto);
        club = clubRepository.save(club);
        return clubMapper.toDTO(club);
    }

    @Transactional
    public ClubDTO update(Long id, ClubDTO dto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", id));

        // Validar nombre único si cambió
        if (!club.getName().equalsIgnoreCase(dto.getName()) &&
                clubRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException(
                    "A club with this name already exists",
                    "name",
                    dto.getName()
            );
        }

        club.setName(dto.getName());
        club.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) {
            club.setIsActive(dto.getIsActive());
        }

        club = clubRepository.save(club);
        return clubMapper.toDTO(club);
    }

    @Transactional
    public void delete(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", id));

        // Verificar que no tenga equipos activos
        if (teamRepository.existsByClubIdAndIsActiveTrue(id)) {
            throw new BusinessException(
                    "Cannot delete club with active teams",
                    "CLUB_HAS_ACTIVE_TEAMS"
            );
        }

        // Soft delete
        club.setIsActive(false);
        clubRepository.save(club);
    }
}