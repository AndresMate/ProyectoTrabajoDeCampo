'use client';

import {useState, useEffect} from 'react';
import teamsService from '@/services/teamsService';
import {tournamentsService} from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';
import clubsService from '@/services/clubsService';
import { toastWarning, toastPromise } from '@/utils/toast';

interface TeamFormProps {
    teamId?: number,
    onSuccess: () => void,
    onCancel: () => void,
    tournamentId?: number
}

export default function TeamForm({teamId, onSuccess, onCancel, tournamentId}: TeamFormProps) {
    const [loading, setLoading] = useState(false);
    const [tournaments, setTournaments] = useState<any[]>([]);
    const [categories, setCategories] = useState<any[]>([]);
    const [clubs, setClubs] = useState<any[]>([]);

    const [formData, setFormData] = useState({
        name: '',
        tournamentId: 0,
        categoryId: 0,
        clubId: 0,
    });

    const [errors, setErrors] = useState<any>({});

    useEffect(() => {
        fetchInitialData();
        if (teamId) {
            loadTeam();
        }
    }, [teamId]);

    const fetchInitialData = async () => {
        try {
            const [tournamentsData, clubsData] = await Promise.all([
                tournamentsService.getAll(),
                clubsService.getActive()
            ]);
            setTournaments(tournamentsData);
            setClubs(clubsData);
        } catch (error) {
            console.error('Error al cargar datos:', error);
        }
    };

    const loadTeam = async () => {
        setLoading(true);
        try {
            const data = await teamsService.getById(teamId!);
            setFormData({
                name: data.name || '',
                tournamentId: data.tournament?.id || 0,
                categoryId: data.category?.id || 0,
                clubId: data.club?.id || 0,
            });

            // Cargar categorías del torneo
            if (data.tournament?.id) {
                fetchCategories(data.tournament.id);
            }
        } catch (error) {
            console.error('Error al cargar equipo:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async (tournamentId: number) => {
        try {
            const tournament = await tournamentsService.getById(tournamentId.toString());
            if (tournament.sport?.id) {
                const categoriesData = await categoriesService.getActiveBySport(tournament.sport.id);
                setCategories(categoriesData);
            }
        } catch (error) {
            console.error('Error al cargar categorías:', error);
        }
    };

    const validateForm = () => {
        const newErrors: any = {};

        if (!formData.name.trim()) {
            newErrors.name = 'El nombre del equipo es requerido';
        }

        if (!formData.tournamentId) {
            newErrors.tournamentId = 'Selecciona un torneo';
        }

        if (!formData.categoryId) {
            newErrors.categoryId = 'Selecciona una categoría';
        }

        if (!formData.clubId) {
            newErrors.clubId = 'Selecciona un club';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const {name, value} = e.target;
        const numericFields = ['tournamentId', 'categoryId', 'clubId'];

        setFormData(prev => ({
            ...prev,
            [name]: numericFields.includes(name) ? Number(value) : value
        }));

        // Si cambió el torneo, cargar las categorías
        if (name === 'tournamentId' && value) {
            fetchCategories(Number(value));
            setFormData(prev => ({...prev, categoryId: 0}));
        }

        if (errors[name]) {
            setErrors((prev: any) => ({...prev, [name]: undefined}));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            toastWarning('Por favor corrige los errores en el formulario');
            return;
        }

        setLoading(true);

        try {
            if (teamId) {
                await toastPromise(
                    teamsService.update(teamId, formData),
                    {
                        loading: 'Actualizando equipo...',
                        success: '✅ Equipo actualizado correctamente',
                        error: (error: any) => error.response?.data?.message || 'Error al actualizar el equipo'
                    }
                );
            } else {
                await toastPromise(
                    teamsService.create(formData),
                    {
                        loading: 'Creando equipo...',
                        success: '✅ Equipo creado correctamente',
                        error: (error: any) => error.response?.data?.message || 'Error al crear el equipo'
                    }
                );
            }
            onSuccess();
        } catch (error: any) {
            console.error('Error al guardar equipo:', error);
            // El error ya se muestra en el toastPromise
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            {loading && <p className="text-center text-gray-500">Cargando...</p>}

            {/* Nombre del equipo */}
            <div>
                <label className="block font-semibold mb-2 text-gray-700">
                    Nombre del Equipo *
                </label>
                <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                        errors.name ? 'border-red-500' : 'border-gray-300'
                    }`}
                    placeholder="Ej: Los Tigres"
                    required
                />
                {errors.name && <p className="text-red-500 text-sm mt-1">{errors.name}</p>}
            </div>

            {/* Torneo */}
            <div>
                <label className="block font-semibold mb-2 text-gray-700">
                    Torneo *
                </label>
                <select
                    name="tournamentId"
                    value={formData.tournamentId}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                        errors.tournamentId ? 'border-red-500' : 'border-gray-300'
                    }`}
                    required
                >
                    <option value="0">Selecciona un torneo</option>
                    {tournaments.map(t => (
                        <option key={t.id} value={t.id}>
                            {t.name} ({t.status})
                        </option>
                    ))}
                </select>
                {errors.tournamentId && (
                    <p className="text-red-500 text-sm mt-1">{errors.tournamentId}</p>
                )}
            </div>

            {/* Categoría */}
            <div>
                <label className="block font-semibold mb-2 text-gray-700">
                    Categoría *
                </label>
                <select
                    name="categoryId"
                    value={formData.categoryId}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                        errors.categoryId ? 'border-red-500' : 'border-gray-300'
                    }`}
                    required
                    disabled={!formData.tournamentId}
                >
                    <option value="0">
                        {formData.tournamentId ? 'Selecciona una categoría' : 'Primero selecciona un torneo'}
                    </option>
                    {categories.map(c => (
                        <option key={c.id} value={c.id}>
                            {c.name}
                        </option>
                    ))}
                </select>
                {errors.categoryId && (
                    <p className="text-red-500 text-sm mt-1">{errors.categoryId}</p>
                )}
            </div>

            {/* Club */}
            <div>
                <label className="block font-semibold mb-2 text-gray-700">
                    Club *
                </label>
                <select
                    name="clubId"
                    value={formData.clubId}
                    onChange={handleChange}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                        errors.clubId ? 'border-red-500' : 'border-gray-300'
                    }`}
                    required
                >
                    <option value="0">Selecciona un club</option>
                    {clubs.map(c => (
                        <option key={c.id} value={c.id}>
                            {c.name}
                        </option>
                    ))}
                </select>
                {errors.clubId && <p className="text-red-500 text-sm mt-1">{errors.clubId}</p>}
            </div>

            {/* Botones */}
            <div className="flex justify-end gap-3 pt-4 border-t">
                <button
                    type="button"
                    onClick={onCancel}
                    className="px-6 py-2 bg-gray-300 rounded-lg hover:bg-gray-400 transition"
                >
                    Cancelar
                </button>
                <button
                    type="submit"
                    disabled={loading}
                    className="px-6 py-2 bg-uptc-black text-white rounded-lg hover:bg-gray-800 transition disabled:opacity-50"
                >
                    {loading ? 'Guardando...' : teamId ? 'Actualizar' : 'Crear'}
                </button>
            </div>
        </form>
    );
}