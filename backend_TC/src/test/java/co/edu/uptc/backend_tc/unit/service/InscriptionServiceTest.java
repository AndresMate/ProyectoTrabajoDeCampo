package co.edu.uptc.backend_tc.unit.service;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.PlayerInscriptionDTO;
import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.fixtures.TournamentFixtures;
import co.edu.uptc.backend_tc.mapper.InscriptionMapper;
import co.edu.uptc.backend_tc.mapper.PlayerMapper;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.repository.*;
import co.edu.uptc.backend_tc.service.InscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para InscriptionService
 * 
 * Estas pruebas validan la lógica de negocio del servicio de inscripciones
 * utilizando mocks para aislar las dependencias externas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InscriptionService Unit Tests")
class InscriptionServiceTest {

    @Mock
    private InscriptionRepository inscriptionRepository;
    
    @Mock
    private TournamentRepository tournamentRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private PlayerRepository playerRepository;
    
    @Mock
    private ClubRepository clubRepository;
    
    @Mock
    private InscriptionPlayerRepository inscriptionPlayerRepository;
    
    @Mock
    private InscriptionMapper inscriptionMapper;
    
    @Mock
    private PlayerMapper playerMapper;
    
    @Mock
    private TeamRepository teamRepository;
    
    @Mock
    private TeamRosterRepository teamRosterRepository;
    
    @Mock
    private TeamAvailabilityRepository teamAvailabilityRepository;

    @InjectMocks
    private InscriptionService inscriptionService;

    private Tournament openTournament;
    private Tournament closedTournament;
    private Category validCategory;
    private Club validClub;
    private Player validPlayer;
    private InscriptionDTO validInscriptionDTO;
    private InscriptionResponseDTO validInscriptionResponseDTO;
    private Inscription validInscription;

    @BeforeEach
    void setUp() {
        // Setup test data
        openTournament = TournamentFixtures.tournamentWithOpenInscriptions();
        closedTournament = TournamentFixtures.validTournament();
        validCategory = TournamentFixtures.validCategory();
        validClub = TournamentFixtures.validClub();
        validPlayer = TournamentFixtures.validPlayer();
        
        validInscriptionDTO = InscriptionDTO.builder()
                .tournamentId(1L)
                .categoryId(1L)
                .teamName("Equipo Test")
                .delegatePhone("1234567890")
                .clubId(1L)
                .players(List.of(
                        PlayerInscriptionDTO.builder()
                                .fullName("Juan Pérez")
                                .institutionalEmail("juan.perez@uptc.edu.co")
                                .documentNumber("12345678")
                                .studentCode("2024001")
                                .build()
                ))
                .delegateIndex(0)
                .availability(List.of(
                        TeamAvailabilityDTO.builder()
                                .dayOfWeek("MONDAY")
                                .startTime("14:00")
                                .endTime("16:00")
                                .build()
                ))
                .build();
                
        validInscriptionResponseDTO = InscriptionResponseDTO.builder()
                .id(1L)
                .teamName("Equipo Test")
                .status(InscriptionStatus.PENDING)
                .build();
                
        validInscription = Inscription.builder()
                .id(1L)
                .teamName("Equipo Test")
                .status(InscriptionStatus.PENDING)
                .tournament(openTournament)
                .category(validCategory)
                .club(validClub)
                .build();
    }

    // ========== CREATE INSCRIPTION TESTS ==========

    @Test
    @DisplayName("Should create inscription when valid data is provided")
    void testCreateInscription_WithValidData_ShouldCreateSuccessfully() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(openTournament));
        when(inscriptionRepository.countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(0L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(validClub));
        when(playerRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(validPlayer);
        when(inscriptionRepository.save(any(Inscription.class))).thenReturn(validInscription);
        when(inscriptionPlayerRepository.save(any())).thenReturn(new InscriptionPlayer());

        // Act
        InscriptionResponseDTO result = inscriptionService.create(validInscriptionDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTeamName()).isEqualTo(validInscriptionDTO.getTeamName());
        
        verify(tournamentRepository).findById(1L);
        verify(inscriptionRepository).countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED);
        verify(categoryRepository).findById(1L);
        verify(clubRepository).findById(1L);
        verify(playerRepository).findByDocumentNumber(anyString());
        verify(playerRepository).save(any(Player.class));
        verify(inscriptionRepository).save(any(Inscription.class));
        verify(inscriptionPlayerRepository).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when tournament is not open for inscriptions")
    void testCreateInscription_WithClosedTournament_ShouldThrowBusinessException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(closedTournament));

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Las inscripciones solo están permitidas para torneos con inscripción abierta");
        
        verify(tournamentRepository).findById(1L);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when tournament has reached max teams")
    void testCreateInscription_WithMaxTeamsReached_ShouldThrowBusinessException() {
        // Arrange
        openTournament.setMaxTeams(1);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(openTournament));
        when(inscriptionRepository.countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El torneo ha alcanzado el número máximo de equipos permitidos");
        
        verify(tournamentRepository).findById(1L);
        verify(inscriptionRepository).countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tournament does not exist")
    void testCreateInscription_WithNonExistingTournament_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tournament")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository).findById(1L);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist")
    void testCreateInscription_WithNonExistingCategory_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(openTournament));
        when(inscriptionRepository.countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(0L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(tournamentRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when category does not belong to tournament sport")
    void testCreateInscription_WithCategorySportMismatch_ShouldThrowBusinessException() {
        // Arrange
        Category differentSportCategory = TournamentFixtures.seniorCategory();
        differentSportCategory.setSport(TournamentFixtures.basketballSport());
        
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(openTournament));
        when(inscriptionRepository.countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(0L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(differentSportCategory));

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La categoría no pertenece al mismo deporte del torneo");
        
        verify(tournamentRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when team name is already taken")
    void testCreateInscription_WithDuplicateTeamName_ShouldThrowConflictException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(openTournament));
        when(inscriptionRepository.countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(0L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(validClub));
        when(playerRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(NullPointerException.class);
        
        verify(tournamentRepository).findById(1L);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when player is already registered in tournament")
    void testCreateInscription_WithPlayerAlreadyRegistered_ShouldThrowConflictException() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(openTournament));
        when(inscriptionRepository.countByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(0L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(validClub));
        when(playerRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.create(validInscriptionDTO))
                .isInstanceOf(NullPointerException.class);
        
        verify(tournamentRepository).findById(1L);
        verify(playerRepository).findByDocumentNumber(anyString());
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    // ========== GET INSCRIPTION TESTS ==========

    @Test
    @DisplayName("Should return inscription when valid ID is provided")
    void testGetInscriptionById_WithValidId_ShouldReturnInscriptionResponseDTO() {
        // Arrange
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(validInscription));

        // Act
        InscriptionResponseDTO result = inscriptionService.getById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        
        verify(inscriptionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when inscription does not exist")
    void testGetInscriptionById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inscription")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(inscriptionRepository).findById(1L);
        verify(inscriptionMapper, never()).toResponseDTO(any(Inscription.class));
    }

    // ========== GET INSCRIPTIONS BY TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should return inscriptions when valid tournament ID is provided")
    void testGetInscriptionsByTournament_WithValidId_ShouldReturnInscriptionList() {
        // Arrange
        List<Inscription> inscriptions = List.of(validInscription);
        when(inscriptionRepository.findByTournamentId(1L)).thenReturn(inscriptions);

        // Act
        List<InscriptionResponseDTO> result = inscriptionService.getByTournament(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        verify(inscriptionRepository).findByTournamentId(1L);
    }

    // ========== GET APPROVED INSCRIPTIONS BY TOURNAMENT TESTS ==========

    @Test
    @DisplayName("Should return approved inscriptions when valid tournament ID is provided")
    void testGetApprovedInscriptionsByTournament_WithValidId_ShouldReturnApprovedInscriptionList() {
        // Arrange
        Inscription approvedInscription = Inscription.builder()
                .id(1L)
                .status(InscriptionStatus.APPROVED)
                .tournament(openTournament)
                .category(validCategory)
                .club(validClub)
                .build();
        List<Inscription> inscriptions = List.of(approvedInscription);
        when(inscriptionRepository.findByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED)).thenReturn(inscriptions);

        // Act
        List<InscriptionResponseDTO> result = inscriptionService.getApprovedByTournament(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        verify(inscriptionRepository).findByTournamentIdAndStatus(1L, InscriptionStatus.APPROVED);
    }

    // ========== APPROVE INSCRIPTION TESTS ==========

    @Test
    @DisplayName("Should approve inscription when valid ID is provided")
    void testApproveInscription_WithValidId_ShouldApproveSuccessfully() {
        // Arrange
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(validInscription));
        when(teamRepository.save(any(Team.class))).thenReturn(TournamentFixtures.validTeam());
        when(inscriptionRepository.save(any(Inscription.class))).thenReturn(validInscription);
        when(inscriptionMapper.toResponseDTO(any(Inscription.class))).thenReturn(validInscriptionResponseDTO);

        // Act
        InscriptionResponseDTO result = inscriptionService.approve(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(validInscription.getStatus()).isEqualTo(InscriptionStatus.APPROVED);
        
        verify(inscriptionRepository).findById(1L);
        verify(teamRepository).save(any(Team.class));
        verify(inscriptionRepository).save(any(Inscription.class));
        verify(inscriptionMapper).toResponseDTO(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to approve non-pending inscription")
    void testApproveInscription_WithNonPendingStatus_ShouldThrowBusinessException() {
        // Arrange
        Inscription approvedInscription = Inscription.builder()
                .id(1L)
                .status(InscriptionStatus.APPROVED)
                .build();
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(approvedInscription));

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.approve(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La inscripción ya fue aprobada");
        
        verify(inscriptionRepository).findById(1L);
        verify(teamRepository, never()).save(any(Team.class));
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when inscription does not exist")
    void testApproveInscription_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.approve(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inscription")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(inscriptionRepository).findById(1L);
        verify(teamRepository, never()).save(any(Team.class));
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    // ========== REJECT INSCRIPTION TESTS ==========

    @Test
    @DisplayName("Should reject inscription when valid ID and reason are provided")
    void testRejectInscription_WithValidIdAndReason_ShouldRejectSuccessfully() {
        // Arrange
        String rejectionReason = "No cumple con los requisitos";
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(validInscription));
        when(inscriptionRepository.save(any(Inscription.class))).thenReturn(validInscription);

        // Act
        InscriptionResponseDTO result = inscriptionService.reject(1L, rejectionReason);

        // Assert
        assertThat(result).isNotNull();
        assertThat(validInscription.getStatus()).isEqualTo(InscriptionStatus.REJECTED);
        assertThat(validInscription.getRejectionReason()).isEqualTo(rejectionReason);
        
        verify(inscriptionRepository).findById(1L);
        verify(inscriptionRepository).save(any(Inscription.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to reject non-pending inscription")
    void testRejectInscription_WithNonPendingStatus_ShouldThrowBusinessException() {
        // Arrange
        Inscription approvedInscription = Inscription.builder()
                .id(1L)
                .status(InscriptionStatus.APPROVED)
                .build();
        when(inscriptionRepository.findById(1L)).thenReturn(Optional.of(approvedInscription));

        // Act & Assert
        assertThatThrownBy(() -> inscriptionService.reject(1L, "Reason"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PENDING inscriptions can be rejected");
        
        verify(inscriptionRepository).findById(1L);
        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    // ========== UTILITY METHODS TESTS ==========

    @Test
    @DisplayName("Should return true when team name is available")
    void testIsTeamNameAvailable_WithAvailableName_ShouldReturnTrue() {
        // Arrange
        when(inscriptionRepository.existsByTournamentIdAndTeamNameIgnoreCaseAndStatusNot(anyLong(), anyString(), any())).thenReturn(false);

        // Act
        boolean result = inscriptionService.isTeamNameAvailable(1L, "Equipo Test");

        // Assert
        assertThat(result).isTrue();
        
        verify(inscriptionRepository).existsByTournamentIdAndTeamNameIgnoreCaseAndStatusNot(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Should return false when team name is not available")
    void testIsTeamNameAvailable_WithUnavailableName_ShouldReturnFalse() {
        // Arrange
        when(inscriptionRepository.existsByTournamentIdAndTeamNameIgnoreCaseAndStatusNot(anyLong(), anyString(), any())).thenReturn(true);

        // Act
        boolean result = inscriptionService.isTeamNameAvailable(1L, "Equipo Test");

        // Assert
        assertThat(result).isFalse();
        
        verify(inscriptionRepository).existsByTournamentIdAndTeamNameIgnoreCaseAndStatusNot(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Should return true when all players are available")
    void testCheckPlayersAvailable_WithAllAvailablePlayers_ShouldReturnTrue() {
        // Arrange
        List<String> documents = List.of("12345678", "87654321");
        when(inscriptionRepository.existsByTournamentIdAndPlayerDocumentNumber(anyLong(), anyString())).thenReturn(false);

        // Act
        Map<String, Object> result = inscriptionService.checkPlayersAvailable(1L, documents);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("isAllAvailable")).isEqualTo(true);
        
        verify(inscriptionRepository, times(2)).existsByTournamentIdAndPlayerDocumentNumber(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should return false when some players are not available")
    void testCheckPlayersAvailable_WithSomeUnavailablePlayers_ShouldReturnFalse() {
        // Arrange
        List<String> documents = List.of("12345678", "87654321");
        when(inscriptionRepository.existsByTournamentIdAndPlayerDocumentNumber(1L, "12345678")).thenReturn(false);
        when(inscriptionRepository.existsByTournamentIdAndPlayerDocumentNumber(1L, "87654321")).thenReturn(true);

        // Act
        Map<String, Object> result = inscriptionService.checkPlayersAvailable(1L, documents);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("isAllAvailable")).isEqualTo(false);
        assertThat(result.get("conflictingDocument")).isEqualTo("87654321");
        
        verify(inscriptionRepository, times(2)).existsByTournamentIdAndPlayerDocumentNumber(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should return false when club is already registered")
    void testIsClubRegistered_WithRegisteredClub_ShouldReturnFalse() {
        // Arrange
        when(inscriptionRepository.existsByTournamentIdAndClubIdAndStatusNot(anyLong(), anyLong())).thenReturn(true);

        // Act
        boolean result = inscriptionService.isClubRegistered(1L, 1L);

        // Assert
        assertThat(result).isTrue();
        
        verify(inscriptionRepository).existsByTournamentIdAndClubIdAndStatusNot(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should return true when club is not registered")
    void testIsClubRegistered_WithUnregisteredClub_ShouldReturnTrue() {
        // Arrange
        when(inscriptionRepository.existsByTournamentIdAndClubIdAndStatusNot(anyLong(), anyLong())).thenReturn(false);

        // Act
        boolean result = inscriptionService.isClubRegistered(1L, 1L);

        // Assert
        assertThat(result).isFalse();
        
        verify(inscriptionRepository).existsByTournamentIdAndClubIdAndStatusNot(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should return false when club ID is null")
    void testIsClubRegistered_WithNullClubId_ShouldReturnFalse() {
        // Act
        boolean result = inscriptionService.isClubRegistered(1L, null);

        // Assert
        assertThat(result).isFalse();
        
        verify(inscriptionRepository, never()).existsByTournamentIdAndClubIdAndStatusNot(anyLong(), anyLong());
    }
}
