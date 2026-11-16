package com.aura.eventos.api.dto;

public class EvidenciaDTOs {
    public record EvidenciaDto(
        String id, String eventFolio, String type, String photoPath,
        String comment, Double latitude, Double longitude, Float accuracyMeters,
        Long capturedAt, String status, Long syncedAt, Integer attempts, String lastError
    ) {}
}
