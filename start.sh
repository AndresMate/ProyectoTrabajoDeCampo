#!/bin/bash

# UPTC Tournament Management System - Quick Start Script
# Este script facilita el inicio rápido del proyecto

echo "🏆 UPTC Tournament Management System - Quick Start"
echo "=================================================="

# Verificar prerrequisitos
echo "📋 Verificando prerrequisitos..."

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java no está instalado. Por favor instala Java 17+"
    exit 1
else
    echo "✅ Java encontrado: $(java -version 2>&1 | head -n 1)"
fi

# Verificar Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js no está instalado. Por favor instala Node.js 18+"
    exit 1
else
    echo "✅ Node.js encontrado: $(node --version)"
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "⚠️  Maven no encontrado globalmente, usando Maven Wrapper"
fi

echo ""
echo "🚀 Iniciando servicios..."

# Función para ejecutar backend
start_backend() {
    echo "🔧 Iniciando Backend (Spring Boot)..."
    cd backend_TC
    
    # Dar permisos al wrapper si es necesario
    chmod +x mvnw 2>/dev/null
    
    # Ejecutar con perfil de desarrollo
    ./mvnw spring-boot:run -Dspring.profiles.active=dev &
    BACKEND_PID=$!
    
    echo "✅ Backend iniciado (PID: $BACKEND_PID)"
    echo "📍 URL: http://localhost:8080"
    echo "📚 Swagger: http://localhost:8080/swagger-ui.html"
    
    cd ..
}

# Función para ejecutar frontend
start_frontend() {
    echo "🎨 Iniciando Frontend (Next.js)..."
    cd frontend-uptc
    
    # Instalar dependencias si es necesario
    if [ ! -d "node_modules" ]; then
        echo "📦 Instalando dependencias del frontend..."
        npm install
    fi
    
    # Ejecutar frontend
    npm run dev &
    FRONTEND_PID=$!
    
    echo "✅ Frontend iniciado (PID: $FRONTEND_PID)"
    echo "📍 URL: http://localhost:3000"
    
    cd ..
}

# Función para mostrar ayuda
show_help() {
    echo ""
    echo "📖 Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  backend     - Solo iniciar backend"
    echo "  frontend    - Solo iniciar frontend"
    echo "  all         - Iniciar ambos servicios (por defecto)"
    echo "  test        - Ejecutar pruebas"
    echo "  help        - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0              # Iniciar ambos servicios"
    echo "  $0 backend      # Solo backend"
    echo "  $0 test         # Ejecutar pruebas"
}

# Función para ejecutar pruebas
run_tests() {
    echo "🧪 Ejecutando pruebas..."
    cd backend_TC
    
    chmod +x mvnw 2>/dev/null
    
    echo "📊 Ejecutando pruebas unitarias..."
    ./mvnw test -Dtest="**/unit/**/*Test" -Dspring.profiles.active=test
    
    echo ""
    echo "📊 Ejecutando pruebas de integración..."
    ./mvnw test -Dtest="**/integration/**/*Test" -Dspring.profiles.active=test
    
    echo ""
    echo "📈 Generando reporte de cobertura..."
    ./mvnw test -Dspring.profiles.active=test jacoco:report
    
    echo ""
    echo "✅ Pruebas completadas!"
    echo "📊 Reportes disponibles en:"
    echo "   - target/test-reports/"
    echo "   - target/site/jacoco/index.html"
    
    cd ..
}

# Función para limpiar procesos
cleanup() {
    echo ""
    echo "🛑 Deteniendo servicios..."
    
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null
        echo "✅ Backend detenido"
    fi
    
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
        echo "✅ Frontend detenido"
    fi
    
    # Limpiar procesos Java y Node
    pkill -f "spring-boot:run" 2>/dev/null
    pkill -f "next-server" 2>/dev/null
    
    echo "🧹 Limpieza completada"
    exit 0
}

# Configurar trap para limpiar al salir
trap cleanup SIGINT SIGTERM

# Procesar argumentos
case "${1:-all}" in
    "backend")
        start_backend
        echo ""
        echo "⏳ Presiona Ctrl+C para detener el backend"
        wait $BACKEND_PID
        ;;
    "frontend")
        start_frontend
        echo ""
        echo "⏳ Presiona Ctrl+C para detener el frontend"
        wait $FRONTEND_PID
        ;;
    "all")
        start_backend
        sleep 5  # Esperar a que el backend inicie
        start_frontend
        echo ""
        echo "🎉 Ambos servicios iniciados!"
        echo "⏳ Presiona Ctrl+C para detener todos los servicios"
        wait
        ;;
    "test")
        run_tests
        ;;
    "help")
        show_help
        ;;
    *)
        echo "❌ Opción no válida: $1"
        show_help
        exit 1
        ;;
esac
