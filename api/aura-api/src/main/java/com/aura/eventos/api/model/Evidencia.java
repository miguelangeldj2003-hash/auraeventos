package com.aura.eventos.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencias")
public class Evidencia {

    @Id
    @Column(length = 100)
    private String id; // UUID

    @Column(name = "event_folio", nullable = false, length = 50)
    private String eventFolio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvidencia type;

    @Column(name = "photo_path", columnDefinition = "TEXT", nullable = false)
    private String photoPath;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Double latitude;
    private Double longitude;

    @Column(name = "accuracy_meters")
    private Float accuracyMeters;

    @Column(name = "captured_at", nullable = false)
    private Long capturedAt;

    @Column(nullable = false, length = 30)
    private String status = "SYNCED";

    @Column(name = "synced_at")
    private Long syncedAt = System.currentTimeMillis();

    private Integer attempts = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public Evidencia() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventFolio() { return eventFolio; }
    public void setEventFolio(String eventFolio) { this.eventFolio = eventFolio; }

    public TipoEvidencia getType() { return type; }
    public void setType(TipoEvidencia type) { this.type = type; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Float getAccuracyMeters() { return accuracyMeters; }
    public void setAccuracyMeters(Float accuracyMeters) { this.accuracyMeters = accuracyMeters; }

    public Long getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Long capturedAt) { this.capturedAt = capturedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Long syncedAt) { this.syncedAt = syncedAt; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
