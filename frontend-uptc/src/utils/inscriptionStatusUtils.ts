// src/utils/inscriptionStatusUtils.ts

/**
 * Traduce el código de estado de inscripción al texto legible.
 */
export function getStatusText(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'Pendiente';
    case 'APPROVED':
      return 'Aprobada';
    case 'REJECTED':
      return 'Rechazada';
    case 'CANCELLED':
      return 'Cancelada';
    default:
      return 'Desconocido';
  }
}

/**
 * Devuelve la clase de color Tailwind para el badge del estado.
 */
export function getStatusBadge(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800';
    case 'APPROVED':
      return 'bg-green-100 text-green-800';
    case 'REJECTED':
      return 'bg-red-100 text-red-800';
    case 'CANCELLED':
      return 'bg-gray-200 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}
