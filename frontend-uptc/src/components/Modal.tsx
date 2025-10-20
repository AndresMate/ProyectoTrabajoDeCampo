'use client';

import { ReactNode } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

export default function Modal({ isOpen, onClose, title, children, size = 'md' }: ModalProps) {
  if (!isOpen) return null;

  const sizeClasses = {
    sm: 'max-w-md',
    md: 'max-w-2xl',
    lg: 'max-w-4xl',
    xl: 'max-w-6xl'
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in">
      <div
        className={`bg-white rounded-2xl shadow-2xl w-full ${sizeClasses[size]} max-h-[90vh] overflow-hidden animate-scale-in`}
      >
        {/* Header del modal con identidad UPTC */}
        <div className="sticky top-0 bg-uptc-black border-b-4 border-uptc-yellow px-6 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-uptc-yellow rounded-full flex items-center justify-center">
              <span className="text-uptc-black font-bold text-lg">U</span>
            </div>
            <h2 className="text-xl font-bold text-uptc-yellow">{title}</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-uptc-yellow transition-colors text-3xl font-bold leading-none w-8 h-8 flex items-center justify-center"
            aria-label="Cerrar modal"
          >
            ×
          </button>
        </div>

        {/* Contenido del modal */}
        <div className="p-6 overflow-y-auto max-h-[calc(90vh-100px)]">
          {children}
        </div>

        {/* Footer decorativo */}
        <div className="sticky bottom-0 h-1 bg-gradient-to-r from-transparent via-uptc-yellow to-transparent"></div>
      </div>
    </div>
  );
}