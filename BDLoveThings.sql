-- Creación de la base de datos
CREATE DATABASE IF NOT EXISTS LoveThings;
USE LoveThings;

-- Tabla Usuarios
CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla Roles
CREATE TABLE Roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name ENUM('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL
);

-- Tabla User Roles
CREATE TABLE User_Roles (
    user_id BIGINT(20) NOT NULL,                   
    role_id INT(11) NOT NULL,                      
    PRIMARY KEY (user_id, role_id),               
    FOREIGN KEY (user_id) REFERENCES Users(id),    
    FOREIGN KEY (role_id) REFERENCES Roles(id)     
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
    FOREIGN KEY (id_usuario) REFERENCES Users(id), 
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
    FOREIGN KEY (id_usuario) REFERENCES Users(id), 
    FOREIGN KEY (id_registro) REFERENCES Registros(id_registro)
);

