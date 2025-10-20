// frontend-uptc/src/services/index.ts
// Exportación centralizada de todos los servicios

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

// Exportar tipos comunes
export type {
  User,
  UsersResponse,
  UserCreateDTO,
  UserUpdateDTO,
  ChangePasswordDTO
} from './usersService';

export type {
  Tournament,
  TournamentCreateDTO,
  PageResponse
} from './tournamentsService';

export type {
  InscriptionDTO,
  InscriptionResponseDTO,
  InscriptionPlayerDTO
} from './inscriptionsService';

export type {
  Sport,
  SportCreateDTO
} from './sportsService';

export type {
  Category,
  CategoryCreateDTO
} from './categoriesService';

export type {
  Match,
  MatchCreateDTO,
  MatchResult
} from './matchesService';

export type {
  Standing
} from './standingsService';

export type {
  Team,
  TeamCreateDTO,
  TeamPlayer
} from './teamsService';

export type {
  Player,
  PlayerCreateDTO,
  PlayerFilterDTO
} from './playersService';

export type {
  Club,
  ClubCreateDTO
} from './clubsService';

export type {
  Venue,
  Scenario,
  VenueCreateDTO,
  ScenarioCreateDTO
} from './venuesService';