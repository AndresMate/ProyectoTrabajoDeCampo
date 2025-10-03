package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.VenueDTO;
import co.edu.uptc.backend_tc.entity.Venue;

public class VenueMapper {

    public static VenueDTO toDTO(Venue venue) {
        return VenueDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .build();
    }

    public static Venue toEntity(VenueDTO dto) {
        return Venue.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
    }
}
