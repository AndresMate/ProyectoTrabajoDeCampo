package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/standings/{tournamentId}/{categoryId}")
    public ResponseEntity<byte[]> exportStandingsExcel(@PathVariable Long tournamentId, @PathVariable Long categoryId) throws Exception {
        ByteArrayInputStream in = reportService.generateStandingsExcel(tournamentId, categoryId);
        byte[] content = in.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=standings.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/inscriptions/{tournamentId}")
    public ResponseEntity<byte[]> exportInscriptionsExcel(@PathVariable Long tournamentId) throws Exception {
        ByteArrayInputStream in = reportService.generateInscriptionsExcel(tournamentId);
        byte[] content = in.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inscriptions.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

}
