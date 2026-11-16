package com.aura.eventos.api.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FotoStorage {
    String guardarFoto(Long articuloId, MultipartFile foto);
    Resource obtenerFoto(Long articuloId);
}
