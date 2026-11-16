package com.aura.eventos.api.service;

import com.aura.eventos.api.dto.EvidenciaDTOs.EvidenciaDto;
import com.aura.eventos.api.model.Evidencia;
import com.aura.eventos.api.model.TipoEvidencia;
import com.aura.eventos.api.repository.EvidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final String uploadDir = System.getProperty("user.home") + "/aura_uploads/evidencias/";

    public EvidenciaService(EvidenciaRepository evidenciaRepository) {
        this.evidenciaRepository = evidenciaRepository;
    }

    public EvidenciaDto subirEvidencia(
            String id, String folio, String tipo, String comentario,
            Double latitud, Double longitud, Float precision, Long capturadoEn, MultipartFile foto
    ) {
        // Idempotencia: si ya existe por UUID, devuelve la existente
        var existente = evidenciaRepository.findById(id);
        if (existente.isPresent()) {
            return toDto(existente.get());
        }

        String pathLocal = "";
        if (foto != null && !foto.isEmpty()) {
            try {
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                File dest = new File(dir, "evi-" + id + ".jpg");
                foto.transferTo(dest);
                pathLocal = dest.getAbsolutePath();
            } catch (IOException e) {
                pathLocal = "foto_local_" + id;
            }
        }

        Evidencia e = new Evidencia();
        e.setId(id);
        e.setEventFolio(folio);
        e.setType(TipoEvidencia.valueOf(tipo));
        e.setPhotoPath(pathLocal);
        e.setComment(comentario);
        e.setLatitude(latitud);
        e.setLongitude(longitud);
        e.setAccuracyMeters(precision);
        e.setCapturedAt(capturadoEn != null ? capturadoEn : System.currentTimeMillis());
        e.setStatus("SYNCED");
        e.setSyncedAt(System.currentTimeMillis());

        Evidencia guardada = evidenciaRepository.save(e);
        return toDto(guardada);
    }

    public EvidenciaDto toDto(Evidencia e) {
        return new EvidenciaDto(
                e.getId(), e.getEventFolio(), e.getType().name(), e.getPhotoPath(),
                e.getComment(), e.getLatitude(), e.getLongitude(), e.getAccuracyMeters(),
                e.getCapturedAt(), e.getStatus(), e.getSyncedAt(), e.getAttempts(), e.getLastError()
        );
    }
}
