package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.ScenarioDTO;
import co.edu.uptc.backend_tc.entity.Scenario;
import co.edu.uptc.backend_tc.entity.Venue;

public class ScenarioMapper {

    public static ScenarioDTO toDTO(Scenario scenario) {
        return ScenarioDTO.builder()
                .id(scenario.getId())
                .name(scenario.getName())
                .capacity(scenario.getCapacity())
                .supportsNightGames(scenario.getSupportsNightGames())
                .venueId(scenario.getVenue().getId())
                .build();
    }

    public static Scenario toEntity(ScenarioDTO dto, Venue venue) {
        return Scenario.builder()
                .id(dto.getId())
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .supportsNightGames(dto.getSupportsNightGames() != null ? dto.getSupportsNightGames() : false)
                .venue(venue)
                .build();
    }
}
