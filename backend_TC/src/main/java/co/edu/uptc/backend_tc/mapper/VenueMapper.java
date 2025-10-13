package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.VenueDTO;
import co.edu.uptc.backend_tc.dto.response.VenueSummaryDTO;
import co.edu.uptc.backend_tc.entity.Venue;
import org.springframework.stereotype.Component;

@Component
public class VenueMapper {

    public VenueDTO toDTO(Venue entity) {
        if (entity == null) return null;

        return VenueDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .build();
    }

    public Venue toEntity(VenueDTO dto) {
        if (dto == null) return null;

        return Venue.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
    }

    public VenueSummaryDTO toSummaryDTO(Venue entity) {
        if (entity == null) return null;

        return VenueSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .build();
    }
}