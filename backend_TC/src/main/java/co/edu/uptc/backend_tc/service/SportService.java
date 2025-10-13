package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.SportDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.SportMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SportService {

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<SportDTO> getAll(Pageable pageable) {
        Page<Sport> page = sportRepository.findAll(pageable);
        return mapperUtils.mapPage(page, sportMapper::toDTO);
    }

    public List<SportDTO> getAllActive() {
        return mapperUtils.mapList(
                sportRepository.findByIsActiveTrue(),
                sportMapper::toDTO
        );
    }

    public SportDTO getById(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", id));
        return sportMapper.toDTO(sport);
    }

    @Transactional
    public SportDTO create(SportDTO dto) {
        // Validar nombre único
        if (sportRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException(
                    "A sport with this name already exists",
                    "name",
                    dto.getName()
            );
        }

        Sport sport = sportMapper.toEntity(dto);
        sport = sportRepository.save(sport);
        return sportMapper.toDTO(sport);
    }

    @Transactional
    public SportDTO update(Long id, SportDTO dto) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", id));

        // Validar nombre único si cambió
        if (!sport.getName().equalsIgnoreCase(dto.getName()) &&
                sportRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException(
                    "A sport with this name already exists",
                    "name",
                    dto.getName()
            );
        }

        sportMapper.updateEntityFromDTO(dto, sport);
        sport = sportRepository.save(sport);
        return sportMapper.toDTO(sport);
    }

    @Transactional
    public void delete(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", id));

        // Soft delete
        sport.setIsActive(false);
        sportRepository.save(sport);
    }

    @Transactional
    public void hardDelete(Long id) {
        if (!sportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sport", "id", id);
        }
        // Verificar que no tenga categorías o torneos asociados
        // Si los tiene, lanzar BusinessException
        sportRepository.deleteById(id);
    }
}