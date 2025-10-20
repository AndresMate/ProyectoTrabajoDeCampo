export default function HeroSection() {
  return (
    <section className="w-full bg-gradient-to-br from-uptc-black via-gray-900 to-uptc-black text-white py-24 px-6 relative overflow-hidden">
      {/* Decoración de fondo */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-20 left-10 w-72 h-72 bg-uptc-yellow rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 right-10 w-96 h-96 bg-uptc-yellow rounded-full blur-3xl"></div>
      </div>

      {/* Contenido */}
      <div className="max-w-5xl mx-auto text-center relative z-10">
        {/* Logo grande */}
        <div className="mb-8 flex justify-center">
          <div className="w-24 h-24 bg-uptc-yellow rounded-full flex items-center justify-center shadow-2xl border-4 border-white">
            <span className="text-uptc-black font-bold text-4xl">U</span>
          </div>
        </div>

        {/* Título principal */}
        <h1 className="text-5xl md:text-6xl font-bold mb-4 text-uptc-yellow">
          Sistema de Torneos UPTC
        </h1>

        {/* Subtítulo */}
        <h2 className="text-2xl md:text-3xl font-semibold mb-6 text-white">
          Universidad Pedagógica y Tecnológica de Colombia
        </h2>

        {/* Descripción */}
        <p className="text-lg md:text-xl mb-8 max-w-3xl mx-auto text-gray-300 leading-relaxed">
          Administra, participa y consulta la información de los torneos deportivos
          de la UPTC. Una plataforma moderna para la gestión integral de competencias deportivas.
        </p>

        {/* Llamados a la acción */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <a
            href="/torneos"
            className="btn-uptc-secondary text-lg px-8 py-4 shadow-xl hover:scale-105 transition-transform"
          >
            🏆 Ver Torneos Activos
          </a>
          <a
            href="/login"
            className="btn-uptc-primary text-lg px-8 py-4 shadow-xl hover:scale-105 transition-transform"
          >
            📝 Iniciar Sesión
          </a>
        </div>

        {/* Estadísticas rápidas */}
        <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-white bg-opacity-10 backdrop-blur-lg rounded-xl p-6 border border-uptc-yellow border-opacity-30">
            <div className="text-4xl font-bold text-uptc-yellow mb-2">+50</div>
            <div className="text-gray-300">Torneos Anuales</div>
          </div>
          <div className="bg-white bg-opacity-10 backdrop-blur-lg rounded-xl p-6 border border-uptc-yellow border-opacity-30">
            <div className="text-4xl font-bold text-uptc-yellow mb-2">+1000</div>
            <div className="text-gray-300">Deportistas</div>
          </div>
          <div className="bg-white bg-opacity-10 backdrop-blur-lg rounded-xl p-6 border border-uptc-yellow border-opacity-30">
            <div className="text-4xl font-bold text-uptc-yellow mb-2">15+</div>
            <div className="text-gray-300">Disciplinas Deportivas</div>
          </div>
        </div>
      </div>

      {/* Onda decorativa inferior */}
      <div className="absolute bottom-0 left-0 right-0">
        <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M0 120L60 105C120 90 240 60 360 45C480 30 600 30 720 37.5C840 45 960 60 1080 67.5C1200 75 1320 75 1380 75L1440 75V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z"
            fill="#FFCC29"
          />
        </svg>
      </div>
    </section>
  );
}