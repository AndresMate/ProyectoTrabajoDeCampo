import type { Metadata } from 'next';
import LayoutWrapper from '@/components/layout/Layout';

export const metadata: Metadata = {
  title: 'Torneos UPTC',
  description: 'Plataforma de torneos universitarios UPTC',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <body>
        <LayoutWrapper>{children}</LayoutWrapper>
      </body>
    </html>
  );
}
