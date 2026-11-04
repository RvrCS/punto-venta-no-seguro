-- ============================================================
-- Punto de venta NO SEGURO - Datos iniciales
-- VULNERABILIDAD: credenciales de ejemplo debilmente protegidas
-- ============================================================

USE punto_venta;

-- ------------------------------------------------------------
-- Usuarios
-- VULNERABILIDAD: passwords en texto plano (facil de reutilizar
-- contra otros servicios, no hay hashing con salt)
-- ------------------------------------------------------------
INSERT INTO usuarios (username, password, nombre, email, rol, activo) VALUES
  ('admin',    'admin',    'Administrador General', 'admin@puntoventa.local',    'ADMIN',   1),
  ('admin2',   '123456',   'Segundo Administrador', 'admin2@puntoventa.local',   'ADMIN',   1),
  ('vendedor', 'vendedor', 'Vendedor Mostrador',    'vendedor@puntoventa.local', 'VENDEDOR', 1),
  ('cajero',   'cajero',   'Cajero Turno Manana',   'cajero@puntoventa.local',   'VENDEDOR', 1);

-- ------------------------------------------------------------
-- Clientes
-- ------------------------------------------------------------
INSERT INTO clientes (documento, nombre, email, telefono, direccion) VALUES
  ('DNI-1001', 'Maria Gomez',    'maria.gomez@correo.com',    '+51 987654321', 'Av. Siempre Viva 742'),
  ('DNI-1002', 'Carlos Ramirez', 'carlos.ramirez@correo.com', '+51 987654322', 'Jr. Los Olivos 128'),
  ('DNI-1003', 'Lucia Fernandez','lucia.fernandez@correo.com','+51 987654323', 'Calle Union 455'),
  ('DNI-1004', 'Jorge Diaz',     'jorge.diaz@correo.com',     '+51 987654324', 'Av. Las Flores 219'),
  ('DNI-1005', 'Ana Torres',     'ana.torres@correo.com',     '+51 987654325', 'Mz. A-Block 15');

-- ------------------------------------------------------------
-- Categorias
-- ------------------------------------------------------------
INSERT INTO categorias (nombre, descripcion) VALUES
  ('Bebidas',   'Gaseosas, jugos, agua y cerveza'),
  ('Abarrotes', 'Arroz, aceite, fideos y azucar'),
  ('Lacteos',   'Leche,quesos y yogures'),
  ('Panaderia', 'Pan, pasteles y galletas'),
  ('Limpieza',  'Detergentes y productos de aseo'),
  ('General',   'Varios');

-- ------------------------------------------------------------
-- Productos
-- VULNERABILIDAD: la descripcion contiene HTML que el frontend
-- renderiza sin sanear -> XSS almacenado
-- ------------------------------------------------------------
INSERT INTO productos (sku, nombre, descripcion, precio, costo, stock, stock_minimo, categoria_id) VALUES
  ('BEB-001', 'Gaseosa Cola 500ml',      'Refresco de cola. <b>Promocion 2x1</b> en combos',           5.50,  3.20, 120, 20, 1),
  ('BEB-002', 'Agua Mineral 625ml',      'Agua mineral sin gas',                                     2.00,  0.90, 200, 30, 1),
  ('BEB-003', 'Jugo de Naranja 1L',      'Jugo 100% natural <script>alert("xss-producto")</script>', 6.75,  4.10, 60,  15, 1),
  ('BEB-004', 'Cerveza Pilsen 330ml',    'Cerveza pilsen importada',                                 8.90,  6.00, 45,  10, 1),
  ('ABA-001', 'Arroz Extra 5kg',         'Arroz de grano largo, bolsa de 5 kilos',                   22.00, 18.50, 35, 10, 2),
  ('ABA-002', 'Aceite Vegetal 1L',       'Aceite vegetal de girasol',                                  14.50, 11.20, 28,  8, 2),
  ('ABA-003', 'Fideos Espagueti 500g',   'Pasta de trigo durum',                                        6.20,  4.50, 90, 20, 2),
  ('ABA-004', 'Azucar Rubia 1kg',        'Azucar rubia refinada',                                      4.80,  3.60, 75, 15, 2),
  ('LAC-001', 'Leche Entera 1L',         'Leche entera larga vida',                                    4.60,  3.40, 70, 20, 3),
  ('LAC-002', 'Queso Fresco 500g',       'Queso fresco blanco',                                        11.00, 8.20, 25,  8, 3),
  ('LAC-003', 'Yogurt Durazno 1L',       'Yogurt sabor durazno',                                        9.30,  6.80, 40, 10, 3),
  ('PAN-001', 'Pan Tajado 500g',         '<b>Pan</b> tajado de molde',3.50,  2.40, 55, 15, 4),
  ('PAN-002', 'Galletas Chocolate',      'Galletas con cobertura de chocolate',                       3.20,  2.00, 110, 25, 4),
  ('LIM-001', 'Detergente 3kg',          'Detergente liquido multiusos',                              18.90, 14.00, 22,  8, 5),
  ('LIM-002', 'Lejia 1L',                'Hipoclorito concentrado',                                    7.40,  5.10, 30, 10, 5),
  ('GEN-001', 'Pilas AA x4',             'Pilas alcalinas AA',                                        12.00,  8.50, 40, 10, 6),
  ('GEN-002', 'Fosforos',                'Caja de fosforos de seguridad',                             2.30,  1.20, 80, 20, 6);

-- ------------------------------------------------------------
-- Ventas de ejemplo (para que los reportes tengan datos)
-- ------------------------------------------------------------
INSERT INTO ventas (folio, cliente_id, usuario_id, subtotal, descuento, impuesto, total, estado, observaciones, creada_en) VALUES
  ('V-2024-0001', 1, 3, 34.20,  0.00, 6.50, 40.70, 'COMPLETADA', 'Venta de mostrador', DATE_SUB(NOW(), INTERVAL 5 DAY)),
  ('V-2024-0002', 2, 4, 47.60,  5.00, 8.12, 50.72, 'COMPLETADA', 'Cliente aplico descuento', DATE_SUB(NOW(), INTERVAL 3 DAY)),
  ('V-2024-0003', 3, 3, 12.80,  0.00, 2.43, 15.23, 'COMPLETADA', '', DATE_SUB(NOW(), INTERVAL 1 DAY)),
  ('V-2024-0004', NULL, 4, 58.30, 0.00, 11.08, 69.38, 'COMPLETADA', 'Consumidor final', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- ------------------------------------------------------------
-- Detalle de las ventas
-- ------------------------------------------------------------
INSERT INTO venta_items (venta_id, producto_id, descripcion, cantidad, precio_unit, subtotal) VALUES
  (1, 1,  'Gaseosa Cola 500ml',  4, 5.50,  22.00),
  (1, 13, 'Galletas Chocolate',  4, 3.05,  12.20),
  (2, 5,  'Arroz Extra 5kg',     2, 22.00, 44.00),
  (2, 12, 'Pan Tajado 500g',     1, 3.60,  3.60),
  (3, 10, 'Queso Fresco 500g',   1, 11.00, 11.00),
  (3, 13, 'Galletas Chocolate',  1, 1.80,  1.80),
  (4, 1,  'Gaseosa Cola 500ml',  6, 5.50,  33.00),
  (4, 4,  'Cerveza Pilsen 330ml',2, 8.90, 17.80),
  (4, 13, 'Galletas Chocolate',  2, 3.75,  7.50);

-- ------------------------------------------------------------
-- Pagos de ejemplo
-- VULNERABILIDAD: PAN y CVV almacenados sin cifrar
-- ------------------------------------------------------------
INSERT INTO pagos (venta_id, metodo, titular, numero_tarjeta, cvv, fecha_vencimiento, monto, estado, referencia) VALUES
  (1, 'TARJETA', 'MARIA GOMEZ',     '4111111111111111', '123', '12/27', 40.70,  'APROBADO', 'TXN-8F3A21C9'),
  (2, 'EFECTIVO','CARLOS RAMIREZ',  NULL,               NULL,  NULL,     50.72,  'APROBADO', 'TXN-2B7E44D1'),
  (3, 'TARJETA', 'LUCIA FERNANDEZ', '5555555555554444', '456', '08/26', 15.23,  'APROBADO', 'TXN-5C1D9A77'),
  (4, 'TARJETA', 'JORGE DIAZ',      '4222222222222',    '321', '03/28', 69.38,  'APROBADO', 'TXN-9E4F60B2');

-- ------------------------------------------------------------
-- Configuracion
-- VULNERABILIDAD: credenciales y claves de servicios guardadas
-- en texto plano dentro de la base de datos
-- ------------------------------------------------------------
INSERT INTO configuracion (clave, valor, categoria) VALUES
  ('pasarela.api.key',      'sk_live_9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c',              'PAGADERA'),
  ('pasarela.secret',       's3cr3t-pasarela-que-nunca-deberia-estar-aqui',          'PAGADERA'),
  ('correo.admin',          'admin@puntoventa.local',                                'CORREO'),
  ('correo.password',       'correo-admin-2024',                                     'CORREO'),
  ('impuesto.porcentaje',   '19.00',                                                'VENTAS'),
  ('tienda.nombre',         'Supermercado El Vendedor',                              'GENERAL'),
  ('tienda.direccion',      'Av. Principal 1234, Ciudad Internacional',              'GENERAL'),
  ('jwt.secret',            'clave-jwt-insegura-de-larga-utilizada-para-firmar',    'SEGURIDAD'),
  ('session.timeout.dias',  '30',                                                   'SEGURIDAD');

-- ------------------------------------------------------------
-- Auditoria
-- VULNERABILIDAD: se registran passwords y datos de tarjeta en claro
-- ------------------------------------------------------------
INSERT INTO auditoria (usuario_id, username, accion, entidad, entidad_id, detalles, ip) VALUES
  (1, 'admin', 'LOGIN', 'SESION', NULL, '{"username":"admin","password":"admin"}', '127.0.0.1'),
  (3, 'vendedor', 'LOGIN', 'SESION', NULL, '{"username":"vendedor","password":"vendedor"}', '127.0.0.1'),
  (1, 'admin', 'LOGOUT', 'SESION', NULL, '{"username":"admin","password":"admin"}', '127.0.0.1');