package com.aura.eventos.api.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ArticuloImagenController {

    private final String uploadDir = System.getProperty("user.home") + "/aura_uploads/articulos/";

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
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir, "art-" + id + ".jpg");
            foto.transferTo(dest);

            String imagenUrl = "/api/articulos/" + id + "/imagen?v=" + System.currentTimeMillis();
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "imagenUrl", imagenUrl,
                    "mensaje", "Foto subida correctamente"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar el archivo en el servidor: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/api/articulos/{id}/imagen", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> obtenerImagen(@PathVariable("id") Long id) {
        File file = new File(uploadDir + "art-" + id + ".jpg");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
