'use client'

import SidebarAdmin from '@/components/layout/SidebarAdmin'
import NavbarAdmin from '@/components/layout/NavbarAdmin'

export default function AdminLayout({children}: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-gray-100">
      <SidebarAdmin />
      <div className="flex-1 ml-64">
        <NavbarAdmin /> {/* se mantiene aquí */}
        <div className="p-6">{children}</div>
      </div>
    </div>
  )
}
