'use client';

import { useState, useEffect } from 'react';
import usersService, { UserCreateDTO, UserUpdateDTO } from '@/services/usersService';

interface UserFormProps {
  userId?: number;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function UserForm({ userId, onSuccess, onCancel }: UserFormProps) {
  const [loading, setLoading] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    role: 'USER',
    password: ''
  });
  const [errors, setErrors] = useState<any>({});

  const roles = [
    { value: 'USER', label: 'Usuario' },
    { value: 'REFEREE', label: 'Árbitro' },
    { value: 'ADMIN', label: 'Administrador' },
    { value: 'SUPER_ADMIN', label: 'Super Administrador' }
  ];

  useEffect(() => {
    if (userId) {
      setIsEdit(true);
      fetchUser();
    }
  }, [userId]);

  const fetchUser = async () => {
    try {
      const data = await usersService.getUserById(userId!);
      setFormData({
        fullName: data.fullName,
        email: data.email,
        role: data.role,
        password: ''
      });
    } catch (error) {
      console.error('Error al cargar usuario:', error);
    }
  };

  const validateForm = () => {
    const newErrors: any = {};

    if (!formData.fullName.trim()) {
      newErrors.fullName = 'El nombre es requerido';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'El email es requerido';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email inválido';
    }

    if (!isEdit && !formData.password) {
      newErrors.password = 'La contraseña es requerida';
    }

    if (formData.password && formData.password.length < 8) {
      newErrors.password = 'La contraseña debe tener al menos 8 caracteres';
    }

    if (!formData.role) {
      newErrors.role = 'Debes seleccionar un rol';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      alert('Por favor corrige los errores en el formulario');
      return;
    }

    setLoading(true);

    try {
      if (isEdit) {
        const updateData: UserUpdateDTO = {
          fullName: formData.fullName,
          email: formData.email,
          role: formData.role
        };
        await usersService.updateUser(userId!, updateData);
        alert('Usuario actualizado exitosamente');
      } else {
        const createData: UserCreateDTO = {
          fullName: formData.fullName,
          email: formData.email,
          role: formData.role,
          password: formData.password
        };
        await usersService.createUser(createData);
        alert('Usuario creado exitosamente');
      }
      onSuccess();
    } catch (error: any) {
      console.error('Error al guardar usuario:', error);
      alert(error.response?.data?.message || 'Error al guardar el usuario');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    if (errors[name]) {
      setErrors((prev: any) => ({ ...prev, [name]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Nombre completo */}
      <div>
        <label className="block text-gray-700 font-medium mb-2">
          Nombre Completo *
        </label>
        <input
          type="text"
          name="fullName"
          value={formData.fullName}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.fullName ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Juan Pérez García"
          required
        />
        {errors.fullName && <p className="text-red-500 text-sm mt-1">{errors.fullName}</p>}
      </div>

      {/* Email */}
      <div>
        <label className="block text-gray-700 font-medium mb-2">
          Correo Electrónico *
        </label>
        <input
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.email ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="usuario@uptc.edu.co"
          required
          disabled={isEdit}
        />
        {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
        {isEdit && <p className="text-sm text-gray-500 mt-1">El email no se puede modificar</p>}
      </div>

      {/* Rol */}
      <div>
        <label className="block text-gray-700 font-medium mb-2">
          Rol *
        </label>
        <select
          name="role"
          value={formData.role}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.role ? 'border-red-500' : 'border-gray-300'
          }`}
          required
        >
          {roles.map(role => (
            <option key={role.value} value={role.value}>
              {role.label}
            </option>
          ))}
        </select>
        {errors.role && <p className="text-red-500 text-sm mt-1">{errors.role}</p>}
      </div>

      {/* Contraseña */}
      {!isEdit && (
        <div>
          <label className="block text-gray-700 font-medium mb-2">
            Contraseña *
          </label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.password ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Mínimo 8 caracteres"
            required={!isEdit}
          />
          {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
          <p className="text-sm text-gray-500 mt-1">
            La contraseña debe tener al menos 8 caracteres
          </p>
        </div>
      )}

      {isEdit && (
        <div className="bg-blue-50 border-l-4 border-blue-500 p-4">
          <p className="text-sm text-blue-700">
            <strong>Nota:</strong> Para cambiar la contraseña, usa la opción "Resetear contraseña"
            desde la lista de usuarios.
          </p>
        </div>
      )}

      {/* Botones */}
      <div className="flex gap-4 pt-4 border-t">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-blue-900 text-white py-3 rounded-lg font-semibold hover:bg-blue-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Guardando...' : isEdit ? 'Actualizar Usuario' : 'Crear Usuario'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition"
        >
          Cancelar
        </button>
      </div>
    </form>
  );
}