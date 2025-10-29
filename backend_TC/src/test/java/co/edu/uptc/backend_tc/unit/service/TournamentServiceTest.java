package co.edu.uptc.backend_tc.unit.service;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.filter.TournamentFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.dto.stats.TournamentStatsDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.fixtures.TournamentFixtures;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.mapper.TournamentMapper;
import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.repository.*;
import co.edu.uptc.backend_tc.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para TournamentService
 * 
 * Estas pruebas validan la lógica de negocio del servicio de torneos
 * utilizando mocks para aislar las dependencias externas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentService Unit Tests")
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;
    
    @Mock
    private SportRepository sportRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TeamRepository teamRepository;
    
    @Mock
    private MatchRepository matchRepository;
    
    @Mock
    private TournamentMapper tournamentMapper;
    
    @Mock
    private MapperUtils mapperUtils;

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament validTournament;
    private TournamentDTO validTournamentDTO;
    private TournamentResponseDTO validTournamentResponseDTO;
    private Sport validSport;
    private Category validCategory;
    private User validUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        validTournament = TournamentFixtures.validTournament();
        validSport = TournamentFixtures.validSport();
        validCategory = TournamentFixtures.validCategory();
        validUser = TournamentFixtures.adminUser();
        
        validTournamentDTO = TournamentDTO.builder()
                .name("Torneo Test")
                .maxTeams(8)
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(60))
                .modality(Modality.DIURNO)
                .sportId(1L)
                .categoryId(1L)
                .createdById(1L)
                .build();
                
        validTournamentResponseDTO = TournamentResponseDTO.builder()
                .id(1L)
                .name("Torneo Test")
                .maxTeams(8)
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(60))
                .modality(Modality.DIURNO)
                .status(TournamentStatus.PLANNING)
                .build();
    }

    // ========== CREATE TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should create tournament when valid data is provided")
    void testCreateTournament_WithValidData_ShouldReturnTournamentResponseDTO() {
        // Arrange
        when(sportRepository.findById(1L)).thenReturn(Optional.of(validSport));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(validTournament);
        when(tournamentMapper.toResponseDTO(any(Tournament.class))).thenReturn(validTournamentResponseDTO);

        // Act
        TournamentResponseDTO result = tournamentService.create(validTournamentDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(validTournamentDTO.getName());
        assertThat(result.getStatus()).isEqualTo(TournamentStatus.PLANNING);
        
        verify(sportRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(tournamentRepository).save(any(Tournament.class));
        verify(tournamentMapper).toResponseDTO(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when start date is after end date")
    void testCreateTournament_WithInvalidDates_ShouldThrowBadRequestException() {
        // Arrange
        TournamentDTO invalidDTO = TournamentDTO.builder()
                .name("Torneo Test")
                .startDate(LocalDate.now().plusDays(60))
                .endDate(LocalDate.now().plusDays(30))
                .modality(Modality.DIURNO)
                .sportId(1L)
                .categoryId(1L)
                .createdById(1L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.create(invalidDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start date must be before end date");
        
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when sport does not exist")
    void testCreateTournament_WithNonExistingSport_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(sportRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.create(validTournamentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sport")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist")
    void testCreateTournament_WithNonExistingCategory_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(sportRepository.findById(1L)).thenReturn(Optional.of(validSport));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.create(validTournamentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when category does not belong to sport")
    void testCreateTournament_WithCategorySportMismatch_ShouldThrowBusinessException() {
        // Arrange
        Category differentSportCategory = TournamentFixtures.seniorCategory();
        differentSportCategory.setSport(TournamentFixtures.basketballSport());
        
        when(sportRepository.findById(1L)).thenReturn(Optional.of(validSport));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(differentSportCategory));

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.create(validTournamentDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Category does not belong to the selected sport");
        
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist")
    void testCreateTournament_WithNonExistingUser_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(sportRepository.findById(1L)).thenReturn(Optional.of(validSport));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.create(validTournamentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ========== GET TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should return tournament when valid ID is provided")
    void testGetTournamentById_WithValidId_ShouldReturnTournamentResponseDTO() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(validTournament));
        when(tournamentMapper.toResponseDTO(any(Tournament.class))).thenReturn(validTournamentResponseDTO);

        // Act
        TournamentResponseDTO result = tournamentService.getById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        
        verify(tournamentRepository).findById(1L);
        verify(tournamentMapper).toResponseDTO(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tournament does not exist")
    void testGetTournamentById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tournament")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository).findById(1L);
        verify(tournamentMapper, never()).toResponseDTO(any(Tournament.class));
    }

    // ========== GET ALL TOURNAMENTS TESTS ==========

    @Test
    @DisplayName("Should return paginated tournaments")
    void testGetAllTournaments_ShouldReturnPageResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Tournament> tournaments = TournamentFixtures.createTournamentList(3);
        Page<Tournament> tournamentPage = new PageImpl<>(tournaments, pageable, 3);
        PageResponseDTO<TournamentResponseDTO> expectedPageResponse = new PageResponseDTO<>();
        
        when(tournamentRepository.findAll(pageable)).thenReturn(tournamentPage);
        when(mapperUtils.mapPage(any(Page.class), any())).thenReturn(expectedPageResponse);

        // Act
        PageResponseDTO<TournamentResponseDTO> result = tournamentService.getAll(pageable);

        // Assert
        assertThat(result).isNotNull();
        
        verify(tournamentRepository).findAll(pageable);
        verify(mapperUtils).mapPage(any(Page.class), any());
    }

    // ========== SEARCH TOURNAMENTS TESTS ==========

    @Test
    @DisplayName("Should return filtered tournaments when search criteria is provided")
    void testSearchTournaments_WithFilters_ShouldReturnFilteredResults() {
        // Arrange
        TournamentFilterDTO filter = new TournamentFilterDTO();
        filter.setName("Test");
        filter.setStatus(TournamentStatus.PLANNING);
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Tournament> tournaments = TournamentFixtures.createTournamentList(2);
        Page<Tournament> tournamentPage = new PageImpl<>(tournaments, pageable, 2);
        PageResponseDTO<TournamentResponseDTO> expectedPageResponse = new PageResponseDTO<>();
        
        when(tournamentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tournamentPage);
        when(mapperUtils.mapPage(any(Page.class), any())).thenReturn(expectedPageResponse);

        // Act
        PageResponseDTO<TournamentResponseDTO> result = tournamentService.search(filter, pageable);

        // Assert
        assertThat(result).isNotNull();
        
        verify(tournamentRepository).findAll(any(Specification.class), eq(pageable));
        verify(mapperUtils).mapPage(any(Page.class), any());
    }

    // ========== DELETE TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should delete tournament when valid ID is provided and no matches exist")
    void testDeleteTournament_WithValidIdAndNoMatches_ShouldDeleteSuccessfully() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(validTournament));
        when(matchRepository.countByTournamentId(1L)).thenReturn(0L);

        // Act
        tournamentService.delete(1L);

        // Assert
        verify(tournamentRepository).findById(1L);
        verify(matchRepository).countByTournamentId(1L);
        verify(tournamentRepository).delete(validTournament);
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to delete tournament with played matches")
    void testDeleteTournament_WithPlayedMatches_ShouldThrowBusinessException() {
        // Arrange
        Tournament inProgressTournament = TournamentFixtures.inProgressTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(inProgressTournament));
        when(matchRepository.countByTournamentId(1L)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete tournament with matches played");
        
        verify(tournamentRepository).findById(1L);
        verify(matchRepository).countByTournamentId(1L);
        verify(tournamentRepository, never()).delete(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tournament does not exist")
    void testDeleteTournament_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tournament")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository, never()).delete(any(Tournament.class));
    }

    // ========== COMPLETE TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should complete tournament when all matches are finished")
    void testCompleteTournament_WithAllMatchesFinished_ShouldCompleteSuccessfully() {
        // Arrange
        Tournament inProgressTournament = TournamentFixtures.inProgressTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(inProgressTournament));
        when(matchRepository.countByTournamentId(1L)).thenReturn(10L);
        when(matchRepository.countByTournamentIdAndStatus(eq(1L), any())).thenReturn(10L);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(inProgressTournament);
        when(tournamentMapper.toResponseDTO(any(Tournament.class))).thenReturn(validTournamentResponseDTO);

        // Act
        TournamentResponseDTO result = tournamentService.completeTournament(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(inProgressTournament.getStatus()).isEqualTo(TournamentStatus.FINISHED);
        
        verify(tournamentRepository).findById(1L);
        verify(matchRepository, atLeastOnce()).countByTournamentId(1L);
        verify(matchRepository, atLeastOnce()).countByTournamentIdAndStatus(eq(1L), any());
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when tournament is not in progress")
    void testCompleteTournament_WithNonInProgressStatus_ShouldThrowBusinessException() {
        // Arrange
        Tournament planningTournament = TournamentFixtures.validTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(planningTournament));

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.completeTournament(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only tournaments in progress can be completed");
        
        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when there are pending matches")
    void testCompleteTournament_WithPendingMatches_ShouldThrowBusinessException() {
        // Arrange
        Tournament inProgressTournament = TournamentFixtures.inProgressTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(inProgressTournament));
        when(matchRepository.countByTournamentId(1L)).thenReturn(10L);
        when(matchRepository.countByTournamentIdAndStatus(eq(1L), any())).thenReturn(8L);

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.completeTournament(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot complete tournament: there are pending matches");
        
        verify(tournamentRepository).findById(1L);
        verify(matchRepository).countByTournamentId(1L);
        verify(matchRepository).countByTournamentIdAndStatus(eq(1L), any());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ========== CANCEL TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should cancel tournament when valid ID is provided")
    void testCancelTournament_WithValidId_ShouldCancelSuccessfully() {
        // Arrange
        Tournament planningTournament = TournamentFixtures.validTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(planningTournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(planningTournament);
        when(tournamentMapper.toResponseDTO(any(Tournament.class))).thenReturn(validTournamentResponseDTO);

        // Act
        TournamentResponseDTO result = tournamentService.cancelTournament(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(planningTournament.getStatus()).isEqualTo(TournamentStatus.CANCELLED);
        
        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to cancel completed tournament")
    void testCancelTournament_WithCompletedTournament_ShouldThrowBusinessException() {
        // Arrange
        Tournament finishedTournament = TournamentFixtures.finishedTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(finishedTournament));

        // Act & Assert
        assertThatThrownBy(() -> tournamentService.cancelTournament(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel completed tournament");
        
        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ========== FIND ACTIVE TOURNAMENTS TESTS ==========

    @Test
    @DisplayName("Should return active tournaments")
    void testFindActiveTournaments_ShouldReturnActiveTournaments() {
        // Arrange
        List<Tournament> activeTournaments = List.of(
                TournamentFixtures.tournamentWithOpenInscriptions(),
                TournamentFixtures.inProgressTournament()
        );
        when(tournamentRepository.findByStatusIn(any())).thenReturn(activeTournaments);
        when(tournamentMapper.toResponseDTO(any(Tournament.class))).thenReturn(validTournamentResponseDTO);

        // Act
        List<TournamentResponseDTO> result = tournamentService.findActiveTournaments();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        verify(tournamentRepository).findByStatusIn(any());
        verify(tournamentMapper, times(2)).toResponseDTO(any(Tournament.class));
    }

    // ========== FIND IN PROGRESS TOURNAMENTS TESTS ==========

    @Test
    @DisplayName("Should return in progress tournaments")
    void testFindInProgressTournaments_ShouldReturnInProgressTournaments() {
        // Arrange
        List<Tournament> inProgressTournaments = List.of(TournamentFixtures.inProgressTournament());
        when(tournamentRepository.findByStatus(TournamentStatus.IN_PROGRESS)).thenReturn(inProgressTournaments);
        when(tournamentMapper.toResponseDTO(any(Tournament.class))).thenReturn(validTournamentResponseDTO);

        // Act
        List<TournamentResponseDTO> result = tournamentService.findInProgressTournaments();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        verify(tournamentRepository).findByStatus(TournamentStatus.IN_PROGRESS);
        verify(tournamentMapper).toResponseDTO(any(Tournament.class));
    }
}
