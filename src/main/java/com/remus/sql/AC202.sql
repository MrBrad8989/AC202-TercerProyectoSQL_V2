-- -----------------------------------------------------
-- Script SQL Completo para AC202 GVS
-- Incluye: Creación BD, Usuario, Tablas, Datos, Función, Trigger
-- -----------------------------------------------------

-- -----------------------------------------------------
-- PARTE 1: CREACIÓN DE BASE DE DATOS Y USUARIO
-- (Puede requerir permisos de administrador/root para ejecutarse)
-- -----------------------------------------------------

-- 1. Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS AC202
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. Crear el usuario 'alumno' si no existe
-- (En MySQL 8+)
CREATE USER IF NOT EXISTS 'alumno'@'%' IDENTIFIED BY 'alumno';

-- 3. Otorgar permisos al usuario sobre la nueva base de datos
GRANT ALL PRIVILEGES ON AC202.* TO 'alumno'@'%';

-- 4. Otorgar permisos para crear funciones y triggers
GRANT CREATE ROUTINE ON AC202.* TO 'alumno'@'%';
GRANT TRIGGER ON AC202.* TO 'alumno'@'%';

-- 5. Recargar privilegios
FLUSH PRIVILEGES;

-- 6. Usar la base de datos AC202 para los siguientes comandos
USE AC202;


-- Tabla `CLIENTES`
CREATE TABLE `CLIENTES` (
  `id_cliente` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  `apellidos` VARCHAR(50) NOT NULL,
  `dni` VARCHAR(20) NULL UNIQUE,
  `telefono` VARCHAR(15) NULL,
  `direccion_habitual` VARCHAR(255) NULL,
  `direccion_envio` VARCHAR(255) NULL,
  `fecha_registro` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `activo` TINYINT(1) NULL DEFAULT 1,
  PRIMARY KEY (`id_cliente`)
);

-- Tabla `PRODUCTOS`
CREATE TABLE `PRODUCTOS` (
  `id_producto` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(50) NOT NULL UNIQUE,
  `descripcion` VARCHAR(255) NULL,
  `precio_recomendado` DECIMAL(10,2) NULL DEFAULT 0.00,
  `stock` INT NULL DEFAULT 0,
  `stock_minimc` INT NULL DEFAULT 0,
  `activo` TINYINT(1) NULL DEFAULT 1,
  `fecha_creacion` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_producto`)
);

-- Tabla `VENTAS`
CREATE TABLE `VENTAS` (
  `id_venta` INT NOT NULL AUTO_INCREMENT,
  `id_cliente` INT NOT NULL,
  `fecha_venta` DATE NULL,
  `descuento_global` DECIMAL(5,2) NULL DEFAULT 0.00,
  `importe_total` DECIMAL(10,2) NULL DEFAULT 0.00,
  `observaciones` TEXT NULL,
  `estado` VARCHAR(20) NULL DEFAULT 'COMPLETADA',
  PRIMARY KEY (`id_venta`),
  CONSTRAINT `fk_VENTAS_CLIENTES`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `CLIENTES` (`id_cliente`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

-- Tabla `LINEAS_VENTA`
CREATE TABLE `LINEAS_VENTA` (
  `id_linea` INT NOT NULL AUTO_INCREMENT,
  `id_venta` INT NOT NULL,
  `id_producto` INT NOT NULL,
  `cantidad` INT NOT NULL,
  `precio_venta` DECIMAL(10,2) NOT NULL,
  `descuento_linea` INT NULL DEFAULT 0,
  `importe_linea` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_linea`),
  CONSTRAINT `fk_LINEAS_VENTA_VENTAS`
    FOREIGN KEY (`id_venta`)
    REFERENCES `VENTAS` (`id_venta`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_LINEAS_VENTA_PRODUCTOS`
    FOREIGN KEY (`id_producto`)
    REFERENCES `PRODUCTOS` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

-- Tabla `EMPRESA`
CREATE TABLE `EMPRESA` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `cif` VARCHAR(20) NOT NULL UNIQUE,
  `nombre` VARCHAR(100) NULL,
  `domicilio` VARCHAR(255) NULL,
  `localidad` VARCHAR(100) NULL,
  `logo_path` VARCHAR(255) NULL,
  `color_principal` VARCHAR(20) NULL,
  `fecha_creacion` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- PARTE 3: INSERCIÓN DE DATOS DE EJEMPLO
-- -----------------------------------------------------

-- Datos para CLIENTES
INSERT INTO `CLIENTES` (`id_cliente`, `nombre`, `apellidos`, `dni`, `telefono`, `direccion_habitual`, `direccion_envio`)
VALUES
(1, 'Cliente', 'De Prueba', '12345678X', '926000000', 'Calle Ficticia 123', 'Calle Ficticia 123'),
(2, 'Ana', 'García López', '87654321Y', '600111222', 'Avenida Principal 45', 'Avenida Principal 45'),
(3, 'Carlos', 'Sánchez Martín', '23456789Z', '611222333', 'Plaza Mayor 1', 'Plaza Mayor 1'),
(4, 'Laura', 'Rodríguez Ruiz', '34567890A', '622333444', 'Calle del Sol 8', 'Apartado de Correos 123'),
(5, 'David', 'Fernández Gómez', '45678901B', '633444555', 'Paseo de la Estación 22', 'Paseo de la Estación 22'),
(6, 'Sofía', 'Pérez Jiménez', '56789012C', '644555666', 'Calle Real 90', 'Calle Real 90'),
(7, 'Javier', 'Moreno Alonso', '67890123D', '655666777', 'Urbanización El Bosque 14', 'Urbanización El Bosque 14'),
(8, 'Lucía', 'Díaz Navarro', '78901234E', '666777888', 'Avenida de la Constitución 11', 'Avenida de la Constitución 11'),
(9, 'Miguel', 'Romero Serrano', '89012345F', '677888999', 'Calle Ancha 5', 'Calle Ancha 5'),
(10, 'Elena', 'Vázquez Castro', '90123456G', '688999000', 'Calle Cervantes 33', 'Calle Cervantes 33');

-- Datos para PRODUCTOS
INSERT INTO `PRODUCTOS` (`id_producto`, `codigo`, `descripcion`, `precio_recomendado`, `stock`, `stock_minimc`)
VALUES
(1, 'PROD-001', 'Teclado Mecánico RGB', 120.50, 100, 10),
(2, 'PROD-002', 'Ratón Óptico Inalámbrico', 45.99, 150, 20),
(3, 'PROD-003', 'Monitor 27" 4K', 350.00, 50, 5),
(4, 'PROD-004', 'SSD NVMe 1TB', 89.90, 80, 15),
(5, 'PROD-005', 'Memoria RAM DDR4 16GB (2x8GB) 3200MHz', 75.00, 120, 25),
(6, 'PROD-006', 'Portátil Gaming 15.6" RTX 4060', 1399.99, 15, 3),
(7, 'PROD-007', 'Tarjeta Gráfica RTX 4070 12GB', 599.90, 20, 5),
(8, 'PROD-008', 'Caja ATX Torre Media Cristal Templado', 85.00, 60, 10),
(9, 'PROD-009', 'Fuente Alimentación 750W 80+ Gold Modular', 110.00, 40, 10),
(10, 'PROD-010', 'Alfombrilla Ratón XL', 24.99, 200, 30),
(11, 'PROD-011', 'Hub USB-C 7 en 1', 39.99, 70, 15),
(12, 'PROD-012', 'Impresora Multifunción WiFi Tinta', 99.90, 30, 10),
(13, 'PROD-013', 'Altavoces 2.1 THX', 149.99, 25, 5),
(14, 'PROD-014', 'Webcam 1080p con Micrófono', 49.95, 80, 20),
(15, 'PROD-015', 'Disco Duro Externo 2TB USB 3.0', 69.90, 100, 15);

-- Datos para EMPRESA
INSERT INTO `EMPRESA` (`id`, `cif`, `nombre`, `domicilio`, `localidad`)
VALUES (1, 'B12345678', 'Mi Tienda de Informática GVS', 'Plaza Mayor 1', 'Ciudad Real');

-- Datos para VENTAS y LINEAS_VENTA
-- VENTA 1: (Cliente 2: Ana García)
INSERT INTO `VENTAS` (`id_venta`, `id_cliente`, `fecha_venta`, `descuento_global`, `importe_total`, `observaciones`, `estado`)
VALUES (1, 2, '2025-10-01', 5.0, 1368.47, 'Pago con tarjeta. Envío urgente.', 'COMPLETADA');
INSERT INTO `LINEAS_VENTA` (`id_venta`, `id_producto`, `cantidad`, `precio_venta`, `descuento_linea`, `importe_linea`)
VALUES
(1, 6, 1, 1399.99, 0, (1 * 1399.99)),
(1, 2, 1, 45.99, 10, (1 * 45.99 * 0.90));

-- VENTA 2: (Cliente 3: Carlos Sánchez)
INSERT INTO `VENTAS` (`id_venta`, `id_cliente`, `fecha_venta`, `descuento_global`, `importe_total`, `observaciones`, `estado`)
VALUES (2, 3, '2025-10-10', 0.0, 329.80, 'Recoge en tienda.', 'COMPLETADA');
INSERT INTO `LINEAS_VENTA` (`id_venta`, `id_producto`, `cantidad`, `precio_venta`, `descuento_linea`, `importe_linea`)
VALUES
(2, 4, 2, 89.90, 0, (2 * 89.90)),
(2, 5, 2, 75.00, 0, (2 * 75.00));

-- VENTA 3: (Cliente 10: Elena Vázquez)
INSERT INTO `VENTAS` (`id_venta
