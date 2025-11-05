package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Generación de reportes en formato Excel")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Generar reporte de tabla de posiciones en Excel", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el archivo")
    })
    @GetMapping("/standings/excel")
    public ResponseEntity<Resource> generateStandingsExcel(
                
                // --- INICIO DE CAMBIOS ---
                
                // 1. Cambiamos los parámetros
                //    Quitamos: @RequestParam Long tournamentId,
                //    Quitamos: @RequestParam Long categoryId
                //    Añadimos:
                @RequestParam List<Long> tournamentIds
                
                // --- FIN DE CAMBIOS ---
        
        ) {
            try {
                // 2. Actualizamos el nombre del archivo (ahora es genérico)
                String filename = "reporte_consolidado_torneos.xlsx";

                // 3. Llamamos al nuevo método del servicio (el que tú creaste)
                ByteArrayInputStream in = reportService.generateStandingsExcel(tournamentIds);
                
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=" + filename);

                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(new InputStreamResource(in));
            } catch (Exception e) {
                // Log the exception e
                return ResponseEntity.status(500).build();
            }
        }

    @Operation(summary = "Generar reporte de inscripciones en Excel", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el archivo")
    })
    @GetMapping("/inscriptions/excel")
    public ResponseEntity<Resource> generateInscriptionsExcel(@RequestParam Long tournamentId) {
        try {
            String filename = "inscriptions_t" + tournamentId + ".xlsx";
            ByteArrayInputStream in = reportService.generateInscriptionsExcel(tournamentId);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            // Log the exception e
            return ResponseEntity.status(500).build();
        }
    }
}