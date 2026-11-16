package com.aura.eventos.api.controller;

import com.aura.eventos.api.dto.EvidenciaDTOs.EvidenciaDto;
import com.aura.eventos.api.service.EvidenciaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaDto> subirEvidencia(
            @RequestParam("id") String id,
            @RequestParam("eventFolio") String eventFolio,
            @RequestParam("type") String type,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "accuracyMeters", required = false) Float accuracyMeters,
            @RequestParam(value = "capturedAt", required = false) Long capturedAt,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        EvidenciaDto dto = evidenciaService.subirEvidencia(
                id, eventFolio, type, comment, latitude, longitude, accuracyMeters, capturedAt, photo
        );
        return ResponseEntity.ok(dto);
    }
}
