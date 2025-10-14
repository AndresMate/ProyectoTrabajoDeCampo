'use client';

const Navbar = () => {
  return (
    <header className="w-full h-16 bg-slate-800 text-white flex items-center justify-between px-6 shadow-md">
      <h1 className="text-lg font-semibold">Sistema de Torneos UPTC</h1>
      <div className="flex items-center gap-4">
        <span className="text-sm">Andrés Mateus</span>
      </div>
    </header>
  );
};

export default Navbar;
