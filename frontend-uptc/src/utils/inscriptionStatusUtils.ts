// frontend-uptc/src/utils/inscriptionStatusUtils.ts

/**
 * Retorna las clases CSS para el badge según el estado
 */
export function getStatusBadge(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800 border border-yellow-300';
    case 'APPROVED':
      return 'bg-green-100 text-green-800 border border-green-300';
    case 'REJECTED':
      return 'bg-red-100 text-red-800 border border-red-300';
    default:
      return 'bg-gray-100 text-gray-800 border border-gray-300';
  }
}

/**
 * Retorna el texto legible del estado
 */
export function getStatusText(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'Pendiente';
    case 'APPROVED':
      return 'Aprobada';
    case 'REJECTED':
      return 'Rechazada';
    default:
      return status;
  }
}

/**
 * Retorna el color del estado para gráficos
 */
export function getStatusColor(status: string): string {
  switch (status) {
    case 'PENDING':
      return '#f59e0b'; // yellow-500
    case 'APPROVED':
      return '#10b981'; // green-500
    case 'REJECTED':
      return '#ef4444'; // red-500
    default:
      return '#6b7280'; // gray-500
  }
}