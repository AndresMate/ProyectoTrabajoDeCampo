import toast from 'react-hot-toast';
import axios, { AxiosError } from 'axios';

/**
 * Utilidades para mostrar notificaciones toast
 */

/**
 * Muestra un mensaje de éxito (verde)
 */
export const toastSuccess = (message: string) => {
  toast.success(message, {
    style: {
      background: '#10b981',
      color: '#fff',
    },
    iconTheme: {
      primary: '#10b981',
      secondary: '#fff',
    },
  });
};

/**
 * Muestra un mensaje de error (rojo)
 */
export const toastError = (message: string) => {
  toast.error(message, {
    style: {
      background: '#ef4444',
      color: '#fff',
    },
    iconTheme: {
      primary: '#ef4444',
      secondary: '#fff',
    },
  });
};

/**
 * Muestra un mensaje de advertencia (amarillo)
 */
export const toastWarning = (message: string) => {
  toast(message, {
    icon: '⚠️',
    style: {
      background: '#F8B500',
      color: '#000',
    },
  });
};

/**
 * Muestra un mensaje informativo (azul)
 */
export const toastInfo = (message: string) => {
  toast(message, {
    icon: 'ℹ️',
    style: {
      background: '#3b82f6',
      color: '#fff',
    },
  });
};

/**
 * Muestra un toast de carga y luego éxito/error basado en la promesa
 * Útil para operaciones async/await
 */
export const toastPromise = <T,>(
  promise: Promise<T>,
  messages: {
    loading: string;
    success: string | ((data: T) => string);
    error: string | ((error: any) => string);
  }
) => {
  return toast.promise(
    promise,
    {
      loading: messages.loading,
      success: (data) => {
        if (typeof messages.success === 'function') {
          return messages.success(data);
        }
        return messages.success;
      },
      error: (error) => {
        if (typeof messages.error === 'function') {
          return messages.error(error);
        }
        return messages.error;
      },
    },
    {
      style: {
        minWidth: '250px',
      },
      success: {
        style: {
          background: '#10b981',
          color: '#fff',
        },
        iconTheme: {
          primary: '#10b981',
          secondary: '#fff',
        },
      },
      error: {
        style: {
          background: '#ef4444',
          color: '#fff',
        },
        iconTheme: {
          primary: '#ef4444',
          secondary: '#fff',
        },
      },
      loading: {
        style: {
          background: '#3b82f6',
          color: '#fff',
        },
        iconTheme: {
          primary: '#3b82f6',
          secondary: '#fff',
        },
      },
    }
  );
};

/**
 * Extrae el mensaje de error de una respuesta de axios
 * Útil para mostrar errores de API de forma consistente
 */
export const getErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string; error?: string }>;
    return (
      axiosError.response?.data?.message ||
      axiosError.response?.data?.error ||
      axiosError.message ||
      'Error al procesar la solicitud'
    );
  }
  
  if (error instanceof Error) {
    return error.message;
  }
  
  return 'Error inesperado';
};

/**
 * Muestra un error de forma estándar desde una respuesta de axios
 */
export const toastAxiosError = (error: unknown) => {
  const message = getErrorMessage(error);
  toastError(message);
};

