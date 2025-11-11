'use client';

import {useState, useEffect} from 'react';
import {tournamentsService} from '@/services/tournamentsService';
// categories selection removed: derive category from the selected tournament or prop
import fixtureService from '@/services/fixtureService';
import { toastWarning, toastPromise } from '@/utils/toast';

interface FixtureGeneratorProps {
    onClose: () => void,
    onSuccess: () => void,
    tournamentId?: number
}

export default function FixtureGenerator({onClose, onSuccess, tournamentId}: FixtureGeneratorProps) {
    const [tournaments, setTournaments] = useState<any[]>([]);
    const [selectedTournament, setSelectedTournament] = useState<number | null>(null);
    const [selectedMode, setSelectedMode] = useState<string>('round_robin');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchTournaments();
    }, []);

    // Si el componente recibe `tournamentId` como prop, preseleccionarlo
    useEffect(() => {
        if (tournamentId) setSelectedTournament(tournamentId);
    }, [tournamentId]);

    // 🔹 Cargar torneos activos
    const fetchTournaments = async () => {
        try {
            const data = await tournamentsService.getAll();
            const filtered = data.filter(
                (t: any) => t.status === 'IN_PROGRESS' || t.status === 'OPEN_FOR_INSCRIPTION'
            );
            setTournaments(filtered);
        } catch (error) {
            console.error('Error al cargar torneos:', error);
        }
    };

    // Note: categories list left for compatibility but we no longer show a category selector

    // 🔹 Generar fixture
    const handleGenerate = async () => {
        const tournamentToUse = selectedTournament ?? tournamentId ?? null;
        const derivedCategoryId = tournamentToUse
            ? (tournaments.find((t: any) => t.id === tournamentToUse)?.category?.id ?? null)
            : null;

        if (!tournamentToUse || !derivedCategoryId) {
            toastWarning('Selecciona torneo (y asegúrate que el torneo tenga una categoría)');
            return;
        }

        setLoading(true);
        try {
            const response = await toastPromise(
                fixtureService.generate(
                    tournamentToUse,
                    derivedCategoryId,
                    selectedMode
                ),
                {
                    loading: 'Generando fixture...',
                    success: (data) => `✅ Fixture generado exitosamente.\n🗓️ Partidos creados: ${data.matchesCreated}`,
                    error: (error: any) => error.response?.data?.message || 'Error al generar el fixture.'
                }
            );
            onSuccess();
        } catch (error: any) {
            console.error('Error generando fixture:', error);
            // El error ya se muestra en el toastPromise
        } finally {
            setLoading(false);
        }
    };

    // 🔹 Eliminar fixture
    const handleDelete = async () => {
        const tournamentToUse = selectedTournament ?? tournamentId ?? null;
        const derivedCategoryId = tournamentToUse
            ? (tournaments.find((t: any) => t.id === tournamentToUse)?.category?.id ?? null)
            : null;

        if (!tournamentToUse || !derivedCategoryId) {
            toastWarning('Selecciona torneo (y asegúrate que el torneo tenga una categoría)');
            return;
        }

        setLoading(true);
        try {
            await toastPromise(
                fixtureService.delete(
                    tournamentToUse,
                    derivedCategoryId
                ),
                {
                    loading: 'Eliminando fixture...',
                    success: '✅ Fixture eliminado exitosamente',
                    error: (error: any) => error.response?.data?.message || 'Error al eliminar fixture.'
                }
            );
            onSuccess();
        } catch (error: any) {
            console.error('Error eliminando fixture:', error);
            // El error ya se muestra en el toastPromise
        } finally {
            setLoading(false);
        }
    };

    // Solo dejamos Round Robin (Eliminación Directa eliminada)
    const modes = [
        {
            value: 'round_robin',
            name: 'Round Robin',
            description: 'Todos los equipos se enfrentan entre sí (ida y vuelta opcional)',
            icon: '🔄',
            recommended: 'Recomendado para ligas',
        }
    ];

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto">
                <div
                    className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
                    <h2 className="text-2xl font-bold text-gray-800">Generador de Fixture</h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-2xl">
                        ×
                    </button>
                </div>

                <div className="p-6 space-y-6">
                    {/* Información */}
                    <div className="bg-blue-50 border-l-4 border-uptc-yellow p-4">
                        <h3 className="font-semibold text-uptc-black mb-2">ℹ️ ¿Qué es un fixture?</h3>
                        <p className="text-sm text-uptc-black">
                            El fixture es el calendario de partidos del torneo. El sistema genera automáticamente
                            los enfrentamientos según el modo seleccionado, considerando la disponibilidad
                            horaria de cada equipo.
                        </p>
                    </div>

                    {/* Selector de torneo */}
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">Seleccionar Torneo *</label>
                        <select
                            value={selectedTournament || ''}
                            onChange={(e) => {
                                setSelectedTournament(Number(e.target.value));
                            }}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow"
                        >
                            <option value="">-- Selecciona un torneo --</option>
                            {tournaments.map((t) => (
                                <option key={t.id} value={t.id}>
                                    {t.name} ({t.status})
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Selector de categoría eliminado: la categoría se deriva del torneo */}

                    {/* Selector de modo */}
                    { (tournamentId || selectedTournament) && (
                        <div>
                            <label className="block text-gray-700 font-medium mb-3">Modo de Fixture *</label>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {modes.map((mode) => (
                                    <div
                                        key={mode.value}
                                        onClick={() => setSelectedMode(mode.value)}
                                        className={`border-2 rounded-lg p-4 cursor-pointer transition ${
                                            selectedMode === mode.value
                                                ? 'border-uptc-yellow bg-blue-50'
                                                : 'border-gray-200 hover:border-uptc-yellow'
                                        }`}
                                    >
                                        <div className="flex items-start gap-3">
                                            <span className="text-3xl">{mode.icon}</span>
                                            <div className="flex-1">
                                                <h3 className="font-semibold text-gray-800 mb-1">{mode.name}</h3>
                                                <p className="text-sm text-gray-600 mb-2">{mode.description}</p>
                                                <span
                                                    className="inline-block text-xs bg-green-100 text-green-700 px-2 py-1 rounded">
                          {mode.recommended}
                        </span>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Botones de acción */}
                    { (tournamentId || selectedTournament) && (
                        <div className="border-t pt-6">
                            <div className="flex gap-3">
                                <button
                                    onClick={handleGenerate}
                                    disabled={loading}
                                    className="flex-1 bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition font-medium disabled:opacity-50"
                                >
                                    {loading ? '⏳ Generando...' : '✨ Generar Fixture'}
                                </button>
                                <button
                                    onClick={handleDelete}
                                    disabled={loading}
                                    className="px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium disabled:opacity-50"
                                >
                                    🗑️ Eliminar Fixture
                                </button>
                            </div>

                            <div className="mt-4 bg-yellow-50 border-l-4 border-yellow-500 p-3">
                                <p className="text-sm text-yellow-800">
                                    <strong>⚠️ Advertencia:</strong> Si ya existe un fixture, será eliminado y
                                    reemplazado
                                    por el nuevo.
                                </p>
                            </div>
                        </div>
                    )}
                </div>

                <div className="border-t p-6 flex justify-end">
                    <button
                        onClick={onClose}
                        className="px-6 py-2 bg-gray-300 rounded-lg hover:bg-gray-400 transition"
                    >
                        Cerrar
                    </button>
                </div>
            </div>
        </div>
    );
}
