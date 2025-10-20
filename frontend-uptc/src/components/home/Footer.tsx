export default function Footer() {
  return (
    <footer className="w-full bg-uptc-black text-white border-t-4 border-uptc-yellow mt-16">
      <div className="max-w-7xl mx-auto px-6 py-12">
        {/* Grid principal */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12 mb-8">
          {/* Columna 1: Logo y descripción */}
          <div>
            <div className="flex items-center gap-3 mb-4">
              <div className="w-12 h-12 bg-uptc-yellow rounded-full flex items-center justify-center">
                <span className="text-uptc-black font-bold text-xl">U</span>
              </div>
              <div>
                <h3 className="text-uptc-yellow font-bold text-lg">UPTC</h3>
                <p className="text-xs text-gray-400">Torneos Deportivos</p>
              </div>
            </div>
            <p className="text-sm text-gray-400 leading-relaxed">
              Universidad Pedagógica y Tecnológica de Colombia.
              Sistema de gestión de torneos deportivos institucionales.
            </p>
          </div>

          {/* Columna 2: Enlaces rápidos */}
          <div>
            <h4 className="text-uptc-yellow font-bold mb-4">Enlaces Rápidos</h4>
            <ul className="space-y-2">
              <li>
                <a href="/" className="text-gray-400 hover:text-uptc-yellow transition-colors text-sm">
                  Inicio
                </a>
              </li>
              <li>
                <a href="/torneos" className="text-gray-400 hover:text-uptc-yellow transition-colors text-sm">
                  Torneos Activos
                </a>
              </li>
              <li>
                <a href="/login" className="text-gray-400 hover:text-uptc-yellow transition-colors text-sm">
                  Iniciar Sesión
                </a>
              </li>
              <li>
                <a href="#" className="text-gray-400 hover:text-uptc-yellow transition-colors text-sm">
                  Reglamentos
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 3: Contacto */}
          <div>
            <h4 className="text-uptc-yellow font-bold mb-4">Contacto</h4>
            <ul className="space-y-2 text-sm text-gray-400">
              <li className="flex items-start gap-2">
                <span>📍</span>
                <span>Avenida Central del Norte, Tunja, Boyacá</span>
              </li>
              <li className="flex items-start gap-2">
                <span>📞</span>
                <span>(+57) 8 7405626</span>
              </li>
              <li className="flex items-start gap-2">
                <span>✉️</span>
                <span>deportes@uptc.edu.co</span>
              </li>
              <li className="flex items-start gap-2">
                <span>🌐</span>
                <a href="https://www.uptc.edu.co" className="hover:text-uptc-yellow transition-colors">
                  www.uptc.edu.co
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Separador */}
        <div className="border-t border-gray-800 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            {/* Copyright */}
            <p className="text-sm text-gray-400 text-center md:text-left">
              © {new Date().getFullYear()} Universidad Pedagógica y Tecnológica de Colombia.
              Todos los derechos reservados.
            </p>

            {/* Advertencia MEN */}
            <div className="flex items-center gap-2">
              <span className="text-xs bg-uptc-yellow text-uptc-black px-3 py-1 rounded-full font-semibold">
                Vigilada Mineducación
              </span>
            </div>
          </div>

          {/* Créditos del equipo */}
          <p className="text-xs text-gray-500 text-center mt-4">
            Desarrollado por Equipo Maravilla • Sistema de Gestión Deportiva UPTC
          </p>
        </div>
      </div>

      {/* Línea decorativa superior */}
      <div className="h-1 bg-gradient-to-r from-transparent via-uptc-yellow to-transparent"></div>
    </footer>
  );
}