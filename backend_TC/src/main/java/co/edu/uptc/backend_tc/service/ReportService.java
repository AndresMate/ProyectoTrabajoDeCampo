package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.repository.InscriptionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    private final StandingService standingService;
    private final InscriptionRepository inscriptionRepository;

    public ReportService(StandingService standingService, InscriptionRepository inscriptionRepository) {
        this.standingService = standingService;
        this.inscriptionRepository = inscriptionRepository;
    }

    // 🔹 Reporte de standings (ya lo tienes)
    public ByteArrayInputStream generateStandingsExcel(Long tournamentId, Long categoryId) throws Exception {
        var standings = standingService.getStandings(tournamentId, categoryId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Standings");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Team", "Points", "Played", "Wins", "Draws", "Losses", "Goals For", "Goals Against"};
            for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1;
            for (var s : standings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getTeamName());
                row.createCell(1).setCellValue(s.getPoints());
                row.createCell(2).setCellValue(s.getPlayed());
                row.createCell(3).setCellValue(s.getWins());
                row.createCell(4).setCellValue(s.getDraws());
                row.createCell(5).setCellValue(s.getLosses());
                row.createCell(6).setCellValue(s.getGoalsFor());
                row.createCell(7).setCellValue(s.getGoalsAgainst());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // 🔹 Reporte de inscripciones
    // Corrige el acceso al delegado y jugadores
    public ByteArrayInputStream generateInscriptionsExcel(Long tournamentId) throws Exception {
        List<Inscription> inscriptions = inscriptionRepository.findByTournamentId(tournamentId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inscriptions");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Team Name", "Delegate", "Phone", "Status", "Category", "Players"};
            for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1;
            for (Inscription ins : inscriptions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ins.getTeamName());
                row.createCell(1).setCellValue(ins.getDelegate() != null ? ins.getDelegate().getFullName() : "");
                row.createCell(2).setCellValue(ins.getDelegatePhone() != null ? ins.getDelegatePhone() : "");
                row.createCell(3).setCellValue(ins.getStatus().name());
                row.createCell(4).setCellValue(ins.getCategory().getName());

                // Si tienes una lista de jugadores, usa ins.getPlayers(), si no, deja vacío
                String players = "No players";
                // row.createCell(5).setCellValue(players);
                row.createCell(5).setCellValue(players);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

}
