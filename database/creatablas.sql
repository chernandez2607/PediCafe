-- PediCafe - esquema relacional compartido.
-- Seleccione primero el schema de destino en MySQL Workbench.
-- Los nombres de tablas, columnas, indices y restricciones usan camelCase.
-- No se insertan datos ni se crean schemas o usuarios de conexion.

-- Roles y usuarios: cada usuario tiene un rol asignado.
CREATE TABLE rol (
    idRol INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(30) NOT NULL,
    descripcion VARCHAR(120) NULL,
    PRIMARY KEY (idRol),
    UNIQUE KEY uqRolNombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
    idUsuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    contrasenaHash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    idRol INT NOT NULL,
    fechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idUsuario),
    UNIQUE KEY uqUsuarioCorreo (correo),
    KEY idxUsuarioRol (idRol),
    CONSTRAINT fkUsuarioRol FOREIGN KEY (idRol)
        REFERENCES rol (idRol) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Catalogo. Las bajas se representan con activo=FALSE; no se borran filas historicas.
CREATE TABLE categoria (
    idCategoria INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idCategoria),
    UNIQUE KEY uqCategoriaNombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto (
    idProducto INT NOT NULL AUTO_INCREMENT,
    idCategoria INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    precio DECIMAL(12,2) UNSIGNED NOT NULL,
    imagenUrl VARCHAR(1024) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idProducto),
    KEY idxProductoCategoriaActivo (idCategoria, activo),
    CONSTRAINT fkProductoCategoria FOREIGN KEY (idCategoria)
        REFERENCES categoria (idCategoria) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Una opcion puede estar disponible para varios productos, con precio por producto.
CREATE TABLE opcionPersonalizacion (
    idOpcion INT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (idOpcion),
    UNIQUE KEY uqOpcionTipoNombre (tipo, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE productoOpcion (
    idProductoOpcion INT NOT NULL AUTO_INCREMENT,
    idProducto INT NOT NULL,
    idOpcion INT NOT NULL,
    precioAdicional DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (idProductoOpcion),
    UNIQUE KEY uqProductoOpcion (idProducto, idOpcion),
    KEY idxProductoOpcionOpcion (idOpcion),
    CONSTRAINT fkProductoOpcionProducto FOREIGN KEY (idProducto)
        REFERENCES producto (idProducto) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkProductoOpcionOpcion FOREIGN KEY (idOpcion)
        REFERENCES opcionPersonalizacion (idOpcion) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Pedidos conserva los importes aplicados y mantiene separado el cliente del usuario que registra.
CREATE TABLE pedido (
    idPedido INT NOT NULL AUTO_INCREMENT,
    numeroPedido VARCHAR(30) NOT NULL,
    tipoPedido VARCHAR(12) NOT NULL COMMENT 'ONLINE o PRESENCIAL; validar en la aplicacion',
    idUsuarioCliente INT NULL,
    idUsuarioRegistra INT NOT NULL,
    fechaPedido DATETIME NOT NULL,
    fechaRetiro DATETIME NULL,
    codigoRetiro VARCHAR(20) NOT NULL,
    estadoActual VARCHAR(20) NOT NULL DEFAULT 'RECIBIDO'
        COMMENT 'RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO o CANCELADO',
    subtotal DECIMAL(12,2) UNSIGNED NOT NULL,
    impuestoPorcentaje DECIMAL(5,2) UNSIGNED NOT NULL,
    impuestoMonto DECIMAL(12,2) UNSIGNED NOT NULL,
    total DECIMAL(12,2) UNSIGNED NOT NULL,
    notas VARCHAR(500) NULL,
    fechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idPedido),
    UNIQUE KEY uqPedidoNumero (numeroPedido),
    UNIQUE KEY uqPedidoCodigoRetiro (codigoRetiro),
    KEY idxPedidoEstadoRetiro (estadoActual, fechaRetiro),
    KEY idxPedidoClienteFecha (idUsuarioCliente, fechaPedido),
    KEY idxPedidoUsuarioRegistraFecha (idUsuarioRegistra, fechaPedido),
    CONSTRAINT fkPedidoCliente FOREIGN KEY (idUsuarioCliente)
        REFERENCES usuario (idUsuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkPedidoUsuarioRegistra FOREIGN KEY (idUsuarioRegistra)
        REFERENCES usuario (idUsuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detallePedido (
    idDetallePedido INT NOT NULL AUTO_INCREMENT,
    idPedido INT NOT NULL,
    idProducto INT NOT NULL,
    nombreProductoSnapshot VARCHAR(100) NOT NULL,
    cantidad SMALLINT UNSIGNED NOT NULL,
    precioUnitarioSnapshot DECIMAL(12,2) UNSIGNED NOT NULL,
    subtotalLinea DECIMAL(12,2) UNSIGNED NOT NULL,
    PRIMARY KEY (idDetallePedido),
    KEY idxDetallePedidoPedido (idPedido),
    KEY idxDetallePedidoProducto (idProducto),
    CONSTRAINT fkDetallePedidoPedido FOREIGN KEY (idPedido)
        REFERENCES pedido (idPedido) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkDetallePedidoProducto FOREIGN KEY (idProducto)
        REFERENCES producto (idProducto) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Snapshots hacen que el historial no cambie si luego se edita una opcion del catalogo.
CREATE TABLE detallePedidoOpcion (
    idDetallePedidoOpcion INT NOT NULL AUTO_INCREMENT,
    idDetallePedido INT NOT NULL,
    idProductoOpcion INT NOT NULL,
    tipoSnapshot VARCHAR(30) NOT NULL,
    nombreSnapshot VARCHAR(80) NOT NULL,
    cantidadOpcion SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    precioAdicionalSnapshot DECIMAL(12,2) UNSIGNED NOT NULL,
    PRIMARY KEY (idDetallePedidoOpcion),
    UNIQUE KEY uqDetallePedidoOpcion (idDetallePedido, idProductoOpcion),
    KEY idxDetallePedidoOpcionCatalogo (idProductoOpcion),
    CONSTRAINT fkDetallePedidoOpcionDetalle FOREIGN KEY (idDetallePedido)
        REFERENCES detallePedido (idDetallePedido) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkDetallePedidoOpcionProductoOpcion FOREIGN KEY (idProductoOpcion)
        REFERENCES productoOpcion (idProductoOpcion) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Puede haber mas de un intento de pago por pedido. No se guardan datos de tarjeta.
CREATE TABLE pago (
    idPago INT NOT NULL AUTO_INCREMENT,
    idPedido INT NOT NULL,
    metodo VARCHAR(20) NOT NULL COMMENT 'TARJETA_SIMULADA o MOSTRADOR; validar en la aplicacion',
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        COMMENT 'PENDIENTE, PAGADO, RECHAZADO o ANULADO; validar en la aplicacion',
    monto DECIMAL(12,2) UNSIGNED NOT NULL,
    fechaPago DATETIME NULL,
    idUsuarioRegistra INT NULL,
    referenciaSimulada VARCHAR(100) NULL,
    PRIMARY KEY (idPago),
    KEY idxPagoPedidoEstado (idPedido, estado),
    KEY idxPagoUsuarioRegistra (idUsuarioRegistra),
    CONSTRAINT fkPagoPedido FOREIGN KEY (idPedido)
        REFERENCES pedido (idPedido) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkPagoUsuarioRegistra FOREIGN KEY (idUsuarioRegistra)
        REFERENCES usuario (idUsuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Registra cada transicion; estadoActual en pedido facilita consultar el estado vigente.
CREATE TABLE pedidoHistorialEstado (
    idHistorial INT NOT NULL AUTO_INCREMENT,
    idPedido INT NOT NULL,
    estadoAnterior VARCHAR(20) NULL,
    estadoNuevo VARCHAR(20) NOT NULL,
    fechaCambio DATETIME NOT NULL,
    idUsuario INT NOT NULL,
    observacion VARCHAR(255) NULL,
    PRIMARY KEY (idHistorial),
    KEY idxPedidoHistorialFecha (idPedido, fechaCambio),
    KEY idxPedidoHistorialUsuario (idUsuario),
    CONSTRAINT fkPedidoHistorialPedido FOREIGN KEY (idPedido)
        REFERENCES pedido (idPedido) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkPedidoHistorialUsuario FOREIGN KEY (idUsuario)
        REFERENCES usuario (idUsuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Existencias actuales. Los movimientos relacionados se registran en movimientoInventario.
CREATE TABLE insumo (
    idInsumo INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    unidadMedida VARCHAR(20) NOT NULL,
    cantidadDisponible DECIMAL(12,3) UNSIGNED NOT NULL DEFAULT 0.000,
    stockMinimo DECIMAL(12,3) UNSIGNED NOT NULL DEFAULT 0.000,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idInsumo),
    UNIQUE KEY uqInsumoNombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE receta (
    idReceta INT NOT NULL AUTO_INCREMENT,
    idProducto INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idReceta),
    UNIQUE KEY uqRecetaProducto (idProducto),
    CONSTRAINT fkRecetaProducto FOREIGN KEY (idProducto)
        REFERENCES producto (idProducto) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalleReceta (
    idDetalleReceta INT NOT NULL AUTO_INCREMENT,
    idReceta INT NOT NULL,
    idInsumo INT NOT NULL,
    cantidadRequerida DECIMAL(12,3) UNSIGNED NOT NULL,
    PRIMARY KEY (idDetalleReceta),
    UNIQUE KEY uqDetalleRecetaInsumo (idReceta, idInsumo),
    KEY idxDetalleRecetaInsumo (idInsumo),
    CONSTRAINT fkDetalleRecetaReceta FOREIGN KEY (idReceta)
        REFERENCES receta (idReceta) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkDetalleRecetaInsumo FOREIGN KEY (idInsumo)
        REFERENCES insumo (idInsumo) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- cantidad siempre es positiva; tipoMovimiento determina si suma o resta existencias.
CREATE TABLE movimientoInventario (
    idMovimiento INT NOT NULL AUTO_INCREMENT,
    idInsumo INT NOT NULL,
    idPedido INT NULL,
    idUsuario INT NULL,
    tipoMovimiento VARCHAR(25) NOT NULL
        COMMENT 'ENTRADA, CONSUMO, REVERSO_CANCELACION o AJUSTE; validar en la aplicacion',
    cantidad DECIMAL(12,3) UNSIGNED NOT NULL,
    fechaMovimiento DATETIME NOT NULL,
    observacion VARCHAR(255) NULL,
    PRIMARY KEY (idMovimiento),
    KEY idxMovimientoInsumoFecha (idInsumo, fechaMovimiento),
    KEY idxMovimientoPedido (idPedido),
    KEY idxMovimientoUsuario (idUsuario),
    CONSTRAINT fkMovimientoInsumo FOREIGN KEY (idInsumo)
        REFERENCES insumo (idInsumo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkMovimientoPedido FOREIGN KEY (idPedido)
        REFERENCES pedido (idPedido) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fkMovimientoUsuario FOREIGN KEY (idUsuario)
        REFERENCES usuario (idUsuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Filas de configuracion usadas por la aplicacion; los pedidos guardan una copia de la tasa aplicada.
CREATE TABLE configuracionSistema (
    idConfiguracion TINYINT NOT NULL,
    impuestoPorcentaje DECIMAL(5,2) UNSIGNED NOT NULL,
    fechaModificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    idUsuarioModifica INT NULL,
    PRIMARY KEY (idConfiguracion),
    KEY idxConfiguracionUsuario (idUsuarioModifica),
    CONSTRAINT fkConfiguracionUsuario FOREIGN KEY (idUsuarioModifica)
        REFERENCES usuario (idUsuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
