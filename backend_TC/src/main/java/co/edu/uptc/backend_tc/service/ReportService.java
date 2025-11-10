package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.InscriptionPlayer;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.repository.InscriptionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final StandingService standingService;
    private final InscriptionRepository inscriptionRepository;

    public ReportService(StandingService standingService, InscriptionRepository inscriptionRepository) {
        this.standingService = standingService;
        this.inscriptionRepository = inscriptionRepository;
    }

    // Este método acepta una lista de IDs de torneo seleccionados desde el frontend
    public ByteArrayInputStream generateStandingsExcel(List<Long> tournamentIds) throws Exception {
        
        // Lista para almacenar las inscripciones de TODOS los torneos seleccionados
        List<Inscription> inscriptions = new ArrayList<>();
        
        // 1. Bucle para obtener inscripciones por cada ID
        for (Long tournamentId : tournamentIds) {
            // CORRECCIÓN: Usamos el método existente findByTournamentId(Long)
            List<Inscription> currentTournamentInscriptions = inscriptionRepository.findByTournamentId(tournamentId);
            inscriptions.addAll(currentTournamentInscriptions);
        }
        
        // --- El resto del código es idéntico al anterior ---

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Participantes Multi-Torneo");

            // 2. Definición de encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Torneo", "Nombre Torneo", "Nombre Completo Participantes", "Código Estudiantil", "No. Identificación", "Correo Electrónico", "Equipo"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1; 
            int nombreTorneoIndex = 1;
            int idTorneoIndex = 0; 

            // 3. Iteración sobre las inscripciones combinadas (la lista 'inscriptions' que ya contiene todo)
            for (Inscription ins : inscriptions) {
                String teamName = ins.getTeamName();
                
                // Accedemos a la información del Torneo (asumiendo que está cargada)
                Long currentTournamentId = ins.getTournament().getId();
                String currentTournamentName = ins.getTournament().getName(); 

                if (ins.getPlayers() != null && !ins.getPlayers().isEmpty()) {
                    
                    for (InscriptionPlayer inscriptionPlayer : ins.getPlayers()) { 
                        
                        Player player = inscriptionPlayer.getPlayer(); 

                        if (player == null) {
                            continue; 
                        }

                        Row row = sheet.createRow(rowIdx++);

                        // Mapeamos los datos del jugador
                        row.createCell(2).setCellValue(player.getFullName()); 
                        row.createCell(3).setCellValue(player.getStudentCode() != null ? player.getStudentCode() : "");
                        row.createCell(4).setCellValue(player.getDocumentNumber());
                        row.createCell(5).setCellValue(player.getInstitutionalEmail() != null ? player.getInstitutionalEmail() : "");
                        row.createCell(6).setCellValue(teamName);
                        
                        // 4. AGREGAMOS NOMBRE E ID DEL TORNEO
                        row.createCell(nombreTorneoIndex).setCellValue(currentTournamentName);
                        row.createCell(idTorneoIndex).setCellValue(currentTournamentId.doubleValue());
                    }
                }
            }

            // 5. Autoajustar las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream generateInscriptionsExcel(Long tournamentId) throws Exception {
    
        // Obtenemos las inscripciones (equipos) para el torneo
        List<Inscription> inscriptions = inscriptionRepository.findByTournamentId(tournamentId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Participantes");

            // 1. Definición de encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Torneo", "Nombre Completo", "Código Estudiantil", "No. Identificación", "Correo Electrónico", "Equipo"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1; // Fila de inicio para los datos

            // 2. Bucle principal sobre las inscripciones (equipos)
            for (Inscription ins : inscriptions) {
                String teamName = ins.getTeamName(); 

                if (ins.getPlayers() != null && !ins.getPlayers().isEmpty()) {
                    
                    // 3. Bucle anidado sobre los InscriptionPlayer de cada equipo
                    for (InscriptionPlayer inscriptionPlayer : ins.getPlayers()) { 
                        
                        // Obtenemos el objeto Player real
                        Player player = inscriptionPlayer.getPlayer(); 

                        if (player == null) {
                            continue; // Saltar si el jugador está nulo por algún error de la DB
                        }

                        // 4. Creamos una fila por CADA jugador
                        Row row = sheet.createRow(rowIdx++);

                        // Mapeamos los datos del jugador (Usando los Getters de la clase Player)
                        row.createCell(0).setCellValue(String.valueOf(tournamentId));
                        row.createCell(1).setCellValue(player.getFullName()); 
                        row.createCell(2).setCellValue(player.getStudentCode() != null ? player.getStudentCode() : "");
                        row.createCell(3).setCellValue(player.getDocumentNumber());
                        row.createCell(4).setCellValue(player.getInstitutionalEmail() != null ? player.getInstitutionalEmail() : "");
                        row.createCell(5).setCellValue(teamName);

                    }
                }
            }

            // 5. Autoajustar las columnas para mejor visualización
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
