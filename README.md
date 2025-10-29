# 🏆 UPTC Tournament Management System

Sistema de gestión de torneos deportivos desarrollado para la Universidad Pedagógica y Tecnológica de Colombia (UPTC). Permite la administración completa de torneos, equipos, inscripciones y partidos.

> 🚀 **Inicio Rápido**: Usa `./start.sh` para iniciar el proyecto automáticamente con un solo comando.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Prerrequisitos](#-prerrequisitos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
  - [Inicio Rápido](#-inicio-rápido-recomendado)
  - [Ejecución Manual](#-ejecución-manual)
- [Testing](#-testing)
- [API Documentation](#-api-documentation)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Contribución](#-contribución)

## ✨ Características

### 🎯 Funcionalidades Principales
- **Gestión de Torneos**: Creación, edición y administración de torneos deportivos
- **Inscripciones**: Sistema completo de inscripción de equipos y jugadores
- **Generación de Fixtures**: Creación automática de calendarios de partidos
- **Gestión de Partidos**: Seguimiento de resultados y estadísticas
- **Sistema de Usuarios**: Roles y permisos (SUPER_ADMIN, ADMIN, REFEREE, PLAYER)
- **Reportes**: Generación de reportes y estadísticas del torneo

### 🔐 Seguridad
- Autenticación JWT
- Autorización basada en roles
- Validación de datos
- Manejo seguro de archivos

## 🛠 Tecnologías

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL** - Base de datos principal (Supabase)
- **H2** - Base de datos para testing
- **Maven** - Gestión de dependencias
- **Swagger/OpenAPI** - Documentación de API

### Frontend
- **Next.js 15.5.5**
- **React 19**
- **TypeScript**
- **Tailwind CSS**
- **Axios** - Cliente HTTP

### Testing
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking framework
- **AssertJ** - Assertions fluidas
- **JaCoCo** - Cobertura de código
- **Spring Boot Test** - Testing de integración

## 📋 Prerrequisitos

### Software Requerido
- **Java 17+** - [Descargar](https://adoptium.net/)
- **Node.js 18+** - [Descargar](https://nodejs.org/)
- **Maven 3.6+** - [Descargar](https://maven.apache.org/)
- **Git** - [Descargar](https://git-scm.com/)

### Servicios Externos
- **Supabase** - Base de datos PostgreSQL (opcional, se puede usar H2 local)
- **Cloudinary** - Almacenamiento de archivos (opcional)

## 🚀 Instalación

### 1. Clonar el Repositorio
```bash
git clone <repository-url>
cd ProyectoTrabajoDeCampo
```

### 2. Configurar Backend
```bash
cd backend_TC
chmod +x mvnw
```

### 3. Configurar Frontend
```bash
cd frontend-uptc
npm install
```

### 4. Dar Permisos al Script de Inicio
```bash
# Desde la raíz del proyecto
chmod +x start.sh
```

### 5. ¡Listo para Ejecutar!
```bash
# Inicio rápido con script automatizado
./start.sh
```

## ⚙️ Configuración

### Backend - Perfiles Disponibles

#### 🔧 Desarrollo Local (Recomendado para desarrollo)
```properties
# Usa H2 en memoria - No requiere Supabase
spring.profiles.active=dev
```

#### 🌐 Producción (Supabase)
```properties
# Usa PostgreSQL en Supabase
spring.profiles.active=supabase
```

### Variables de Entorno
Crear archivo `.env` en `frontend-uptc/`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your_cloudinary_name
```

## 🏃‍♂️ Ejecución

### 🚀 Inicio Rápido (Recomendado)

Usa el script automatizado para iniciar el proyecto fácilmente:

```bash
# Iniciar ambos servicios (backend + frontend)
./start.sh

# Solo backend
./start.sh backend

# Solo frontend
./start.sh frontend

# Ejecutar todas las pruebas
./start.sh test

# Mostrar ayuda
./start.sh help
```

El script automáticamente:
- ✅ Verifica prerrequisitos (Java, Node.js, Maven)
- ✅ Inicia servicios en el orden correcto
- ✅ Muestra URLs de acceso
- ✅ Maneja limpieza al salir (Ctrl+C)

### 🔧 Ejecución Manual

#### Backend

##### Opción 1: Con Maven (Recomendado)
```bash
cd backend_TC

# Desarrollo local (H2)
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Producción (Supabase)
./mvnw spring-boot:run -Dspring.profiles.active=supabase
```

##### Opción 2: JAR Ejecutable
```bash
cd backend_TC

# Compilar
./mvnw clean package -DskipTests

# Ejecutar
java -jar target/backend_TC-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

#### Frontend
```bash
cd frontend-uptc
npm run dev
```

### URLs de Acceso
- **Backend API**: http://localhost:8080
- **Frontend**: http://localhost:3000
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console** (dev): http://localhost:8080/h2-console

## 🧪 Testing

### 📊 Resumen de Pruebas
- **Total de Pruebas**: 57
- **Pruebas Unitarias**: 57 ✅ (100% passing)
- **Pruebas de Integración**: 5 clases implementadas
- **Cobertura de Código**: JaCoCo configurado

### 🚀 Ejecutar Pruebas

#### Todas las Pruebas
```bash
cd backend_TC
./mvnw test -Dspring.profiles.active=test
```

#### Solo Pruebas Unitarias
```bash
./mvnw test -Dtest="**/unit/**/*Test" -Dspring.profiles.active=test
```

#### Solo Pruebas de Integración
```bash
./mvnw test -Dtest="**/integration/**/*Test" -Dspring.profiles.active=test
```

#### Con Reporte de Cobertura
```bash
./mvnw test -Dspring.profiles.active=test jacoco:report
```

### 📈 Cobertura de Pruebas

#### Pruebas Unitarias
- **AuthServiceTest**: 13 tests - Autenticación y autorización
- **TournamentServiceTest**: 20 tests - Gestión de torneos
- **InscriptionServiceTest**: 24 tests - Inscripciones y equipos

#### Pruebas de Integración
- **TournamentIntegrationTest** - Flujo completo de torneos
- **InscriptionManagementIntegrationTest** - Gestión de inscripciones
- **FixtureGenerationIntegrationTest** - Generación de fixtures
- **SecurityIntegrationTest** - Seguridad y autenticación
- **TournamentManagementIntegrationTest** - Administración de torneos

### 📊 Reportes de Testing
- **Reportes Surefire**: `target/test-reports/`
- **Cobertura JaCoCo**: `target/site/jacoco/index.html`
- **Resumen**: `target/test-reports/test-summary.md`

## 📚 API Documentation

### Swagger UI
Accede a la documentación interactiva de la API:
```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principales
- **POST** `/api/auth/login` - Autenticación
- **GET** `/api/tournaments` - Listar torneos
- **POST** `/api/tournaments` - Crear torneo
- **GET** `/api/inscriptions` - Listar inscripciones
- **POST** `/api/inscriptions` - Crear inscripción
- **GET** `/api/matches` - Listar partidos

## 📁 Estructura del Proyecto

```
ProyectoTrabajoDeCampo/
├── backend_TC/                    # Backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/co/edu/uptc/backend_tc/
│   │   │   │   ├── config/       # Configuraciones
│   │   │   │   ├── controller/   # Controladores REST
│   │   │   │   ├── entity/       # Entidades JPA
│   │   │   │   ├── repository/   # Repositorios
│   │   │   │   ├── service/      # Lógica de negocio
│   │   │   │   └── dto/          # Data Transfer Objects
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-dev.properties
│   │   └── test/
│   │       ├── java/co/edu/uptc/backend_tc/
│   │       │   ├── unit/         # Pruebas unitarias
│   │       │   ├── integration/  # Pruebas de integración
│   │       │   └── fixtures/     # Datos de prueba
│   │       └── resources/
│   │           └── application-test.properties
│   ├── pom.xml
│   └── mvnw
├── frontend-uptc/                 # Frontend Next.js
│   ├── src/
│   │   ├── app/                  # App Router
│   │   ├── components/           # Componentes React
│   │   ├── services/             # Servicios API
│   │   └── utils/                # Utilidades
│   ├── package.json
│   └── next.config.ts
└── README.md
```

## 🎯 Roles del Sistema

### 👑 SUPER_ADMIN
- Gestión completa del sistema
- Administración de usuarios
- Configuración global

### 🔧 ADMIN
- Gestión de torneos
- Administración de inscripciones
- Generación de reportes

### ⚖️ REFEREE
- Gestión de partidos
- Registro de resultados
- Control de sanciones

### 🏃‍♂️ PLAYER
- Visualización de torneos
- Inscripción en equipos
- Consulta de resultados

## 🔧 Troubleshooting

### Problemas Comunes

#### Error de Conexión a Base de Datos
```bash
# Usar perfil de desarrollo local
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### Error de Permisos en Maven Wrapper
```bash
chmod +x mvnw
```

#### Problemas con Node.js
```bash
# Limpiar caché
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Logs y Debugging
- **Backend**: Logs en consola con nivel DEBUG
- **Frontend**: Logs en consola del navegador
- **Testing**: Reportes detallados en `target/test-reports/`

## 🤝 Contribución

### Flujo de Trabajo
1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Estándares de Código
- **Java**: Seguir convenciones de Spring Boot
- **TypeScript**: Usar ESLint y Prettier
- **Commits**: Mensajes descriptivos en español
- **Testing**: Mantener cobertura > 80%

## 📄 Licencia

Este proyecto está desarrollado para fines académicos en la Universidad Pedagógica y Tecnológica de Colombia (UPTC).

## 👥 Equipo de Desarrollo

- **Backend**: Spring Boot, Java 17
- **Frontend**: Next.js, React, TypeScript
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Base de Datos**: PostgreSQL (Supabase), H2 (Testing)

---

**Última actualización**: Octubre 2024  
**Versión**: 1.0.0  
**Estado**: ✅ Funcional y Probado