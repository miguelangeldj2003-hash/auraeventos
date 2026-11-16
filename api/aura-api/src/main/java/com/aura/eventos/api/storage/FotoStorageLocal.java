package com.aura.eventos.api.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FotoStorageLocal implements FotoStorage {

    private final String storagePath;

    public FotoStorageLocal(@Value("${aura.storage.path:./fotos}") String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public String guardarFoto(Long articuloId, MultipartFile foto) {
        try {
            File dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, "art-" + articuloId + ".jpg");
            foto.transferTo(dest);
            return "/api/articulos/" + articuloId + "/imagen?v=" + System.currentTimeMillis();
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto en el almacenamiento local: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource obtenerFoto(Long articuloId) {
        File file = new File(storagePath, "art-" + articuloId + ".jpg");
        if (!file.exists()) {
            return null;
        }
        return new FileSystemResource(file);
    }
}
