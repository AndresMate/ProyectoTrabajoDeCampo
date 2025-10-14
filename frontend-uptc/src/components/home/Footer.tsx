export default function Footer() {
  return (
    <footer className="w-full bg-blue-900 text-white text-center py-6 mt-10">
      <p>© {new Date().getFullYear()} Universidad Pedagógica y Tecnológica de Colombia</p>
      <p className="text-sm text-gray-300">Desarrollado por equipo maravilla</p>
    </footer>
  );
}
