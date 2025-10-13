package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.VenueDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.Venue;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.VenueMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.ScenarioRepository;
import co.edu.uptc.backend_tc.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;
    private final ScenarioRepository scenarioRepository;
    private final VenueMapper venueMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<VenueDTO> getAll(Pageable pageable) {
        Page<Venue> page = venueRepository.findAll(pageable);
        return mapperUtils.mapPage(page, venueMapper::toDTO);
    }

    public List<VenueDTO> searchByName(String name) {
        return mapperUtils.mapList(
                venueRepository.findByNameContainingIgnoreCase(name),
                venueMapper::toDTO
        );
    }

    public VenueDTO getById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));
        return venueMapper.toDTO(venue);
    }

    @Transactional
    public VenueDTO create(VenueDTO dto) {
        // Validar nombre único
        if (venueRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException(
                    "Venue with this name already exists",
                    "name",
                    dto.getName()
            );
        }

        Venue venue = venueMapper.toEntity(dto);
        venue = venueRepository.save(venue);
        return venueMapper.toDTO(venue);
    }

    @Transactional
    public VenueDTO update(Long id, VenueDTO dto) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        // Validar nombre único si cambió
        if (!venue.getName().equalsIgnoreCase(dto.getName()) &&
                venueRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException(
                    "Venue with this name already exists",
                    "name",
                    dto.getName()
            );
        }

        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());

        venue = venueRepository.save(venue);
        return venueMapper.toDTO(venue);
    }

    @Transactional
    public void delete(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        // Verificar que no tenga escenarios asociados
        long scenarioCount = scenarioRepository.countByVenueId(id);
        if (scenarioCount > 0) {
            throw new BusinessException(
                    "Cannot delete venue with associated scenarios",
                    "VENUE_HAS_SCENARIOS"
            );
        }

        venueRepository.delete(venue);
    }
}