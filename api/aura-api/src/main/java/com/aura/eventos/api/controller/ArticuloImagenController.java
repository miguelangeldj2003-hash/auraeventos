package com.aura.eventos.api.controller;

import com.aura.eventos.api.storage.FotoStorage;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ArticuloImagenController {

    private final FotoStorage fotoStorage;

    public ArticuloImagenController(FotoStorage fotoStorage) {
        this.fotoStorage = fotoStorage;
    }

    @PostMapping(value = "/api/admin/articulos/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirImagen(
            @PathVariable("id") Long id,
            @RequestParam("foto") MultipartFile foto
    ) {
        if (foto.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo de foto está vacío"));
        }

        if (foto.getSize() > 8 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "La foto excede el tamaño máximo permitido (8 MB)"));
        }

        String contentType = foto.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("image/jpeg")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solo se permiten imágenes en formato image/jpeg"));
        }

        try {
            String imagenUrl = fotoStorage.guardarFoto(id, foto);
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "imagenUrl", imagenUrl,
                    "mensaje", "Foto subida correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar el archivo en el servidor: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/api/articulos/{id}/imagen", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> obtenerImagen(@PathVariable("id") Long id) {
        Resource resource = fotoStorage.obtenerFoto(id);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
