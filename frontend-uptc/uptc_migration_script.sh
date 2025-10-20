#!/bin/bash

# ==============================================
# Script de Migración a Identidad Visual UPTC
# ==============================================
# Este script automatiza el proceso de actualización
# de colores según el Manual de Identidad Gráfica UPTC 2022
#
# Uso: ./migrate-to-uptc-identity.sh
# ==============================================

echo "🎨 Iniciando migración a Identidad Visual UPTC..."
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Contador de cambios
CHANGES=0

# ==============================================
# 1. BACKUP
# ==============================================
echo "📦 Creando backup..."
BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
cp -r src "$BACKUP_DIR/"
echo -e "${GREEN}✓${NC} Backup creado en: $BACKUP_DIR"
echo ""

# ==============================================
# 2. MIGRACIÓN DE COLORES DE FONDO
# ==============================================
echo "🎨 Actualizando colores de fondo..."

# bg-blue-900 → bg-uptc-black
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/bg-blue-900/bg-uptc-black/g' {} \;
COUNT=$(find src/ -name "*.bak" | wc -l)
CHANGES=$((CHANGES + COUNT))
echo -e "${GREEN}✓${NC} bg-blue-900 → bg-uptc-black: $COUNT archivos"

# bg-blue-800 → bg-uptc-black (hover)
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/bg-blue-800/bg-gray-800/g' {} \;
echo -e "${GREEN}✓${NC} bg-blue-800 → bg-gray-800: hover states"

# bg-blue-700 → bg-uptc-black
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/bg-blue-700/bg-uptc-black/g' {} \;
echo -e "${GREEN}✓${NC} bg-blue-700 → bg-uptc-black"

# bg-yellow-400 → bg-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/bg-yellow-400/bg-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} bg-yellow-400 → bg-uptc-yellow"

# bg-yellow-500 → bg-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/bg-yellow-500/bg-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} bg-yellow-500 → bg-uptc-yellow"

echo ""

# ==============================================
# 3. MIGRACIÓN DE COLORES DE TEXTO
# ==============================================
echo "✍️  Actualizando colores de texto..."

# text-blue-900 → text-uptc-black
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/text-blue-900/text-uptc-black/g' {} \;
echo -e "${GREEN}✓${NC} text-blue-900 → text-uptc-black"

# text-blue-800 → text-uptc-black
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/text-blue-800/text-uptc-black/g' {} \;
echo -e "${GREEN}✓${NC} text-blue-800 → text-uptc-black"

# text-yellow-400 → text-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/text-yellow-400/text-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} text-yellow-400 → text-uptc-yellow"

echo ""

# ==============================================
# 4. MIGRACIÓN DE BORDES
# ==============================================
echo "🔲 Actualizando colores de bordes..."

# border-blue-900 → border-uptc-yellow (destacados)
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/border-blue-900/border-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} border-blue-900 → border-uptc-yellow"

# border-blue-500 → border-uptc-yellow (focus)
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/border-blue-500/border-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} border-blue-500 → border-uptc-yellow"

echo ""

# ==============================================
# 5. MIGRACIÓN DE RING (FOCUS STATES)
# ==============================================
echo "💍 Actualizando estados de foco..."

# ring-blue-500 → ring-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/ring-blue-500/ring-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} ring-blue-500 → ring-uptc-yellow"

# ring-blue-900 → ring-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/ring-blue-900/ring-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} ring-blue-900 → ring-uptc-yellow"

echo ""

# ==============================================
# 6. MIGRACIÓN DE HOVER STATES
# ==============================================
echo "👆 Actualizando estados hover..."

# hover:bg-blue-800 → hover:bg-gray-800
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/hover:bg-blue-800/hover:bg-gray-800/g' {} \;
echo -e "${GREEN}✓${NC} hover:bg-blue-800 → hover:bg-gray-800"

# hover:text-blue-900 → hover:text-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/hover:text-blue-900/hover:text-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} hover:text-blue-900 → hover:text-uptc-yellow"

# hover:border-blue-400 → hover:border-uptc-yellow
find src/ -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.jsx" -o -name "*.js" \) \
  -exec sed -i.bak 's/hover:border-blue-400/hover:border-uptc-yellow/g' {} \;
echo -e "${GREEN}✓${NC} hover:border-blue-400 → hover:border-uptc-yellow"

echo ""

# ==============================================
# 7. LIMPIEZA DE ARCHIVOS BACKUP
# ==============================================
echo "🧹 Limpiando archivos temporales..."
find src/ -name "*.bak" -delete
echo -e "${GREEN}✓${NC} Archivos .bak eliminados"
echo ""

# ==============================================
# 8. VERIFICACIÓN
# ==============================================
echo "🔍 Verificando migración..."
echo ""

# Contar referencias a colores antiguos
BLUE_REFS=$(grep -r "blue-[789]00" src/ --include="*.tsx" --include="*.ts" 2>/dev/null | wc -l)
OLD_YELLOW=$(grep -r "yellow-[45]00" src/ --include="*.tsx" --include="*.ts" 2>/dev/null | wc -l)

if [ "$BLUE_REFS" -gt 0 ]; then
    echo -e "${YELLOW}⚠${NC}  Advertencia: Aún hay $BLUE_REFS referencias a colores blue-[789]00"
    echo "   Ejecuta: grep -r 'blue-[789]00' src/"
fi

if [ "$OLD_YELLOW" -gt 0 ]; then
    echo -e "${YELLOW}⚠${NC}  Advertencia: Aún hay $OLD_YELLOW referencias a yellow-[45]00"
    echo "   Ejecuta: grep -r 'yellow-[45]00' src/"
fi

# Verificar que existen las nuevas clases
NEW_UPTC=$(grep -r "uptc-" src/ --include="*.tsx" --include="*.ts" 2>/dev/null | wc -l)
echo -e "${GREEN}✓${NC} Encontradas $NEW_UPTC referencias a clases uptc-*"

echo ""

# ==============================================
# 9. RESUMEN
# ==============================================
echo "📊 RESUMEN DE MIGRACIÓN"
echo "================================"
echo "Archivos modificados: $CHANGES"
echo "Backup guardado en: $BACKUP_DIR"
echo ""
echo -e "${GREEN}✓ Migración completada${NC}"
echo ""
echo "📝 PRÓXIMOS PASOS:"
echo "1. Revisa los cambios con: git diff"
echo "2. Prueba la aplicación: npm run dev"
echo "3. Verifica visualmente todos los componentes"
echo "4. Si hay problemas, restaura desde: $BACKUP_DIR"
echo ""
echo "⚠️  RECORDATORIOS:"
echo "• Actualiza tailwind.config.ts con los colores UPTC"
echo "• Actualiza globals.css con las clases de utilidad"
echo "• Reemplaza componentes de layout según la guía"
echo "• Agrega el logo oficial UPTC"
echo "• Incluye 'Vigilada Mineducación' en footer"
echo ""
echo "📚 Consulta la Guía de Implementación para más detalles"