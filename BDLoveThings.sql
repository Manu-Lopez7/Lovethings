-- Creación de la base de datos
CREATE DATABASE IF NOT EXISTS LoveThings;
USE LoveThings;

-- Tabla Usuarios
CREATE TABLE Usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contraseña VARCHAR(255) NOT NULL,
    rol ENUM('usuario', 'administrador') NOT NULL,
    premium BOOLEAN DEFAULT FALSE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla Categorías
CREATE TABLE Categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT
);

-- Tabla Registros
CREATE TABLE Registros (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    categoria INT,
    ubicacion VARCHAR(255),
    fecha DATE,
    precio DECIMAL(10, 2),
    calificacion INT,
    imagen VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (categoria) REFERENCES Categorias(id_categoria)
);

-- Tabla Platos (Subcategoría de restaurantes)
CREATE TABLE Platos (
    id_plato INT AUTO_INCREMENT PRIMARY KEY,
    id_registro INT,
    nombre_plato VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tiempo_preparado TIME,
    precio DECIMAL(10, 2),
    calificacion INT,
    imagen VARCHAR(255),
    FOREIGN KEY (id_registro) REFERENCES Registros(id_registro)
);

-- Tabla Favoritos
CREATE TABLE Favoritos (
    id_favorito INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    id_registro INT,
    fecha_favorito TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (id_registro) REFERENCES Registros(id_registro)
);

-- Tabla Compartidos
CREATE TABLE Compartidos (
    id_compartido INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_origen INT,
    id_usuario_destino INT,
    id_registro INT,
    fecha_compartido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    mensaje TEXT,
    FOREIGN KEY (id_usuario_origen) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (id_usuario_destino) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (id_registro) REFERENCES Registros(id_registro)
);