package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.ScenarioDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.Scenario;
import co.edu.uptc.backend_tc.entity.Venue;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.ScenarioMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.MatchRepository;
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
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final VenueRepository venueRepository;
    private final MatchRepository matchRepository;
    private final ScenarioMapper scenarioMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<ScenarioDTO> getAll(Pageable pageable) {
        Page<Scenario> page = scenarioRepository.findAll(pageable);
        return mapperUtils.mapPage(page, scenarioMapper::toDTO);
    }

    public List<ScenarioDTO> getByVenue(Long venueId) {
        return mapperUtils.mapList(
                scenarioRepository.findByVenueId(venueId),
                scenarioMapper::toDTO
        );
    }

    public List<ScenarioDTO> getScenariosForNightGames() {
        return mapperUtils.mapList(
                scenarioRepository.findBySupportsNightGamesTrue(),
                scenarioMapper::toDTO
        );
    }

    public ScenarioDTO getById(Long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario", "id", id));
        return scenarioMapper.toDTO(scenario);
    }

    @Transactional
    public ScenarioDTO create(ScenarioDTO dto) {
        // Verificar que el venue existe
        Venue venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", dto.getVenueId()));

        Scenario scenario = Scenario.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .supportsNightGames(dto.getSupportsNightGames() != null ? dto.getSupportsNightGames() : false)
                .venue(venue)
                .build();

        scenario = scenarioRepository.save(scenario);
        return scenarioMapper.toDTO(scenario);
    }

    @Transactional
    public ScenarioDTO update(Long id, ScenarioDTO dto) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario", "id", id));

        // Actualizar venue si cambió
        if (dto.getVenueId() != null && !dto.getVenueId().equals(scenario.getVenue().getId())) {
            Venue venue = venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", dto.getVenueId()));
            scenario.setVenue(venue);
        }

        scenario.setName(dto.getName());
        scenario.setCapacity(dto.getCapacity());
        scenario.setSupportsNightGames(dto.getSupportsNightGames());

        scenario = scenarioRepository.save(scenario);
        return scenarioMapper.toDTO(scenario);
    }

    @Transactional
    public void delete(Long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario", "id", id));

        // Verificar que no tenga partidos programados
        long matchCount = matchRepository.countByScenarioId(id);
        if (matchCount > 0) {
            throw new BusinessException(
                    "Cannot delete scenario with scheduled matches",
                    "SCENARIO_HAS_MATCHES"
            );
        }

        scenarioRepository.delete(scenario);
    }
}