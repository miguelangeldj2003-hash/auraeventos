package com.aura.eventos.api.security;

import com.aura.eventos.api.model.RolUsuario;
import com.aura.eventos.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs = 864000000L; // 10 días

    public JwtService(@Value("${aura.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Padding si la clave es menor a 256 bits
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(Usuario u) {
        return Jwts.builder()
                .subject(u.getCorreo())
                .claim("id", u.getId())
                .claim("rol", u.getRol().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public UsuarioAutenticado validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long id = claims.get("id", Long.class);
            if (id == null) {
                Number idNum = claims.get("id", Number.class);
                id = idNum != null ? idNum.longValue() : null;
            }

            String correo = claims.getSubject();
            String rolStr = claims.get("rol", String.class);
            RolUsuario rol = RolUsuario.valueOf(rolStr);

            return new UsuarioAutenticado(id, correo, rol);
        } catch (Exception e) {
            return null;
        }
    }
}
