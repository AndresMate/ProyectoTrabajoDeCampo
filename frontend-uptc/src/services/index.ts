// typescript
// `src/services/index.ts`
// Reexportación centralizada de servicios y tipos verificados

export { authService } from './authService';
export { tournamentsService } from './tournamentsService';
export { default as usersService } from './usersService';
export { default as inscriptionService } from './inscriptionsService';
export { default as sportsService } from './sportsService';
export { default as categoriesService } from './categoriesService';
export { default as matchesService } from './matchesService';
export { default as standingsService } from './standingsService';
export { default as teamsService } from './teamsService';
export { default as playersService } from './playersService';
export { default as clubsService } from './clubsService';
export { default as venuesService } from './venuesService';
export { default as reportsService } from './reportsService';
export { default as fileUploadService } from './fileUploadService';
export { default as fixtureService } from './fixtureService';
export { default as inscriptionPlayerService } from './inscriptionPlayerService';
export { default as inscriptionsService } from './inscriptionsService';
export { default as sanctionsService } from './sanctionsService';

// Exportar solo los tipos que están presentes en los archivos proporcionados
export type { Category, CategoryCreateDTO } from './categoriesService';
export type { Club } from './clubsService';
export type { InscriptionPlayerDTO, PlayerSummary, InscriptionWithLimit } from './inscriptionPlayerService';
export type {
  PlayerInscriptionDTO,
  TeamAvailabilityDTO,
  InscriptionCreateDTO,
  PlayerSummaryDTO,
  InscriptionResponseDTO
} from './inscriptionsService';
export type { Sport, SportCreateDTO } from './sportsService';
export type { Match, MatchCreateDTO, MatchResult } from './matchesService';
export type { Player, PlayerCreateDTO, PlayerFilterDTO } from './playersService';
export type { Standing } from './standingsService';
