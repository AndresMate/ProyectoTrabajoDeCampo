'use client';

export default function NavbarAdmin() {
  return (
    <header className="w-full h-16 bg-slate-800 text-white flex items-center justify-between px-6 shadow-md">
      <h1 className="text-lg font-semibold">Panel de Administración</h1>
      <div className="flex items-center gap-4">
        <span>Admin</span>
        <button className="bg-red-500 px-3 py-1 rounded hover:bg-red-400">
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}
