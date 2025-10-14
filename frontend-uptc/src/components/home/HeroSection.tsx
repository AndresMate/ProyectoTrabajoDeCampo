export default function HeroSection() {
  return (
    <section className="w-full bg-blue-700 text-white py-20 px-6 text-center">
      <h1 className="text-4xl font-bold mb-4">Bienvenido al Sistema de Torneos UPTC</h1>
      <p className="text-lg mb-6 max-w-2xl mx-auto">
        Administra, participa y consulta la información de los torneos deportivos de la UPTC.
      </p>
      <a
        href="#torneos"
        className="bg-yellow-400 text-blue-900 font-semibold px-6 py-3 rounded-lg hover:bg-yellow-500 transition"
      >
        Ver torneos
      </a>
    </section>
  );
}
