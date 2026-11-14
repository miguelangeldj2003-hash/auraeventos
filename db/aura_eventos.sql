-- ============================================================
-- Base de Datos MySQL: aura_eventos
-- Sistema de Gestión y Renta de Mobiliario / Candy Bar
-- ============================================================

CREATE DATABASE IF NOT EXISTS aura_eventos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aura_eventos;

-- ------------------------------------------------------------
-- Tabla: usuarios
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cuentas de ejemplo por defecto
INSERT INTO usuarios (id, nombre, correo, password, telefono, rol) VALUES
(1, 'Ana Martínez', 'ana@aura.com', '$2a$10$e8T7tZg0H7H1J4x7Q4E.LeXw.X1zK5.H7H1J4x7Q4E.LeXw.X1zK5', '55 1234 5678', 'ADMIN'),
(2, 'María García López', 'maria@gmail.com', '$2a$10$e8T7tZg0H7H1J4x7Q4E.LeXw.X1zK5.H7H1J4x7Q4E.LeXw.X1zK5', '55 9876 5432', 'CLIENTE')
ON DUPLICATE KEY UPDATE id=id;

-- ------------------------------------------------------------
-- Tabla: categorias
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    seccion VARCHAR(50) NOT NULL -- 'MOBILIARIO' o 'MESA_DULCES'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO categorias (id, nombre, seccion) VALUES
(1, 'Sillas & Mesas', 'MOBILIARIO'),
(2, 'Lounges', 'MOBILIARIO'),
(3, 'Iluminación & Letreros', 'MOBILIARIO'),
(4, 'Arcos & Florales', 'MOBILIARIO'),
(5, 'Candy Bar', 'MESA_DULCES'),
(6, 'Stands & Bases', 'MESA_DULCES'),
(7, 'Fondue & Fuentes', 'MESA_DULCES'),
(8, 'Mesas Candy', 'MESA_DULCES')
ON DUPLICATE KEY UPDATE id=id;

-- ------------------------------------------------------------
-- Tabla: articulos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS articulos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio_base DOUBLE NOT NULL,
    stock_total INT NOT NULL DEFAULT 1,
    imagen_url VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    sku VARCHAR(50),
    dimensiones VARCHAR(100),
    material VARCHAR(100),
    incluye TEXT,
    peso_max VARCHAR(50),
    en_mantenimiento BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_articulos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO articulos (id, categoria_id, nombre, descripcion, precio_base, stock_total, sku, dimensiones, material, incluye, peso_max) VALUES
(1, 1, 'Sillas Chiavari Blancas x10', 'Set de 10 sillas con cojín satinado. Perfectas para ceremonias y banquetes formales.', 2400.0, 15, 'SC-004-BLA', '0.92m de alto', 'Madera lacada', 'Cojín satinado', '120 kg'),
(2, 1, 'Mesa Rectangular Rústica', 'Mesa de madera para 8 personas.', 380.0, 12, 'SC-010-RUS', '2.4m × 0.9m', 'Madera de pino', 'Montaje', '150 kg'),
(3, 2, 'Set Lounge Blanco Moderno', 'Sofá 3 plazas, 2 sillones y mesa de centro. Ideal para áreas VIP y lounges de eventos.', 3500.0, 6, 'SL-005-BLA', '3.2m × 1.8m', 'Piel sintética blanca', 'Mesa de centro', '250 kg'),
(4, 3, 'Letrero LED "LOVE" Rosa', 'Letrero neón flexible con cable 3m. Brillo ajustable. Ideal para fondos fotográficos.', 650.0, 5, 'LN-003-ROS', '1.2m × 0.5m', 'Neón LED flexible', 'Cable 3m', '2 kg'),
(8, 5, 'Carrito Vintage para Dulces', 'Carrito de madera blanca con ruedas doradas. Incluye faldón y decoración base.', 1800.0, 3, 'CB-006-VIN', '1.2m × 0.6m × 1.4m', 'Madera lacada', 'Faldón y decoración', '60 kg'),
(12, 8, 'Mesa Candy Premium Dorada Deluxe', 'Mesa decorativa de madera blanca con herrajes dorados. Perfecta para exposición de postres, dulces y bebidas.', 1200.0, 12, 'MC-001-ORO', '1.8m × 0.6m × 0.9m', 'MDF lacado blanco', 'Montaje + desmontaje', '80 kg')
ON DUPLICATE KEY UPDATE id=id;

-- ------------------------------------------------------------
-- Tabla: reservaciones
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(30) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    fecha_evento DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    dias INT NOT NULL DEFAULT 1,
    direccion TEXT NOT NULL,
    entorno VARCHAR(50) NOT NULL DEFAULT 'INTERIOR',
    notas TEXT,
    latitud DOUBLE,
    longitud DOUBLE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APROBADO, RECHAZADO
    motivo_rechazo TEXT,
    costo_flete DOUBLE NOT NULL DEFAULT 0.0,
    subtotal DOUBLE NOT NULL DEFAULT 0.0,
    iva DOUBLE NOT NULL DEFAULT 0.0,
    total DOUBLE NOT NULL DEFAULT 0.0,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservaciones_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Tabla: detalles_reservacion
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalles_reservacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservacion_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    CONSTRAINT fk_detalles_reservacion FOREIGN KEY (reservacion_id) REFERENCES reservaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalles_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
