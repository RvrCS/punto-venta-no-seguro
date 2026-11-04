-- ============================================================
-- Punto de venta NO SEGURO - Esquema de base de datos
-- Curso de cyberseguridad aplicada
--
-- MySQL 8.4 / InnoDB / utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS punto_venta
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE punto_venta;

-- ------------------------------------------------------------
-- usuarios (administradores y vendedores)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password      VARCHAR(255) NOT NULL COMMENT 'VULNERABILIDAD: se guardara en texto plano o MD5',
  nombre        VARCHAR(100) NOT NULL,
  email         VARCHAR(120),
  rol           VARCHAR(20)  NOT NULL DEFAULT 'VENDEDOR' COMMENT 'ADMIN | VENDEDOR',
  activo        TINYINT(1)   NOT NULL DEFAULT 1,
  creado_en     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME,
  INDEX idx_usuarios_rol (rol)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- clientes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  documento   VARCHAR(20)  NOT NULL UNIQUE,
  nombre      VARCHAR(120) NOT NULL,
  email       VARCHAR(120),
  telefono    VARCHAR(30),
  direccion   VARCHAR(255),
  activo      TINYINT(1)   NOT NULL DEFAULT 1,
  creado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- categorias
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(80) NOT NULL UNIQUE,
  descripcion VARCHAR(255),
  creado_en   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- productos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  sku             VARCHAR(40)  NOT NULL UNIQUE,
  nombre          VARCHAR(150) NOT NULL,
  descripcion     TEXT         COMMENT 'VULNERABILIDAD: se renderiza como HTML en el frontend',
  precio          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  costo           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  stock           INT          NOT NULL DEFAULT 0,
  stock_minimo    INT          NOT NULL DEFAULT 5,
  categoria_id    BIGINT,
  imagen_path     VARCHAR(255) COMMENT 'VULNERABILIDAD: path construido con el nombre enviado por el cliente',
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
  INDEX idx_productos_nombre (nombre),
  INDEX idx_productos_precio (precio)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- ventas (cabecera / factura)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ventas (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  folio          VARCHAR(20) NOT NULL UNIQUE,
  cliente_id     BIGINT,
  usuario_id     BIGINT COMMENT 'vendedor que realizo la venta',
  subtotal       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  descuento      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  impuesto       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total          DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  estado         VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA' COMMENT 'COMPLETADA | ANULADA | PENDIENTE',
  observaciones  VARCHAR(500),
  creada_en      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  creado_en      DATETIME,
  actualizado_en DATETIME,
  CONSTRAINT fk_ventas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
  CONSTRAINT fk_ventas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  INDEX idx_ventas_fecha (creada_en),
  INDEX idx_ventas_cliente (cliente_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- venta_items (detalle)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta_items (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id      BIGINT NOT NULL,
  producto_id   BIGINT,
  descripcion   VARCHAR(150) NOT NULL COMMENT 'copia del nombre al momento de la venta',
  cantidad      INT NOT NULL DEFAULT 1,
  precio_unit   DECIMAL(10,2) NOT NULL COMMENT 'VULNERABILIDAD: precio tomado del request',
  subtotal      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  creado_en      DATETIME,
  actualizado_en DATETIME,
  CONSTRAINT fk_items_venta FOREIGN KEY (venta_id) REFERENCES ventas (id) ON DELETE CASCADE,
  CONSTRAINT fk_items_producto FOREIGN KEY (producto_id) REFERENCES productos (id),
  INDEX idx_items_venta (venta_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- pagos (pasarela simulada)
-- VULNERABILIDAD: datos de tarjeta en claro, sin cifrado en reposo
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pagos (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id          BIGINT NOT NULL,
  metodo            VARCHAR(30) NOT NULL DEFAULT 'TARJETA' COMMENT 'TARJETA | EFECTIVO | TRANSFERENCIA',
  titular           VARCHAR(120),
  numero_tarjeta    VARCHAR(25)  COMMENT 'PAN completo en claro',
  cvv               VARCHAR(10)  COMMENT 'CVV en claro',
  fecha_vencimiento VARCHAR(10),
  monto             DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  estado            VARCHAR(30) NOT NULL DEFAULT 'APROBADO',
  referencia        VARCHAR(60),
  procesado_en      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pagos_venta FOREIGN KEY (venta_id) REFERENCES ventas (id) ON DELETE CASCADE,
  INDEX idx_pagos_venta (venta_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- movimientos de inventario
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimientos_inventario (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id   BIGINT NOT NULL,
  tipo          VARCHAR(20) NOT NULL COMMENT 'ENTRADA | SALIDA | AJUSTE',
  cantidad      INT NOT NULL,
  stock_anterior INT NOT NULL DEFAULT 0,
  stock_nuevo   INT NOT NULL DEFAULT 0,
  usuario_id    BIGINT,
  venta_id      BIGINT,
  motivo        VARCHAR(255),
  registrado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  creado_en      DATETIME,
  actualizado_en DATETIME,
  CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id) REFERENCES productos (id),
  INDEX idx_mov_producto (producto_id),
  INDEX idx_mov_fecha (registrado_en)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- configuracion (VULNERABILIDAD: claves de API en tabla sin cifrar)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS configuracion (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  clave       VARCHAR(80) NOT NULL UNIQUE,
  valor       TEXT COMMENT 'VULNERABILIDAD: secretos en claro, endpoint que la expone',
  categoria   VARCHAR(40) NOT NULL DEFAULT 'GENERAL',
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- auditoria (VULNERABILIDAD: se registran passwords y datos de tarjeta)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id  BIGINT,
  username    VARCHAR(50),
  accion      VARCHAR(80) NOT NULL,
  entidad     VARCHAR(50),
  entidad_id  BIGINT,
  detalles    TEXT COMMENT 'VULNERABILIDAD: payload completo sin sanear',
  ip          VARCHAR(45),
  registrado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_auditoria_usuario (usuario_id),
  INDEX idx_auditoria_fecha (registrado_en)
) ENGINE=InnoDB;

-- VULNERABILIDAD: cuenta con privilegios amplios adicional
CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY 'app123';
GRANT ALL PRIVILEGES ON punto_venta.* TO 'app'@'%';
FLUSH PRIVILEGES;