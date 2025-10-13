package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.ScenarioDTO;
import co.edu.uptc.backend_tc.dto.response.ScenarioSummaryDTO;
import co.edu.uptc.backend_tc.entity.Scenario;
import co.edu.uptc.backend_tc.entity.Venue;
import org.springframework.stereotype.Component;

@Component
public class ScenarioMapper {

    private final VenueMapper venueMapper;

    public ScenarioMapper(VenueMapper venueMapper) {
        this.venueMapper = venueMapper;
    }

    public ScenarioDTO toDTO(Scenario entity) {
        if (entity == null) return null;

        return ScenarioDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .supportsNightGames(entity.getSupportsNightGames())
                .venueId(entity.getVenue() != null ? entity.getVenue().getId() : null)
                .build();
    }

    public ScenarioSummaryDTO toSummaryDTO(Scenario entity) {
        if (entity == null) return null;

        return ScenarioSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .venue(venueMapper.toSummaryDTO(entity.getVenue()))
                .build();
    }
}
