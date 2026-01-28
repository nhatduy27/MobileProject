import { Injectable, Logger, InternalServerErrorException } from '@nestjs/common';
import { ConfigService } from '../../../core/config/config.service';
import axios, { AxiosError } from 'axios';

/**
 * Location for route computation
 */
export interface RouteLocation {
  lat: number;
  lng: number;
}

/**
 * Route computation result
 */
export interface RouteResult {
  /** Total distance in meters */
  distance: number;

  /** Total duration in seconds */
  duration: number;

  /** Encoded polyline (optional) */
  polyline?: string;

  /** Optimized waypoint order (original indices) */
  waypointOrder: number[];
}

/**
 * Google Routes API Service
 *
 * Integrates with Google Routes API for route optimization.
 * Computes optimized routes with multiple waypoints.
 */
@Injectable()
export class GoogleRoutesService {
  private readonly logger = new Logger(GoogleRoutesService.name);
  private readonly apiKey: string;
  private readonly apiUrl = 'https://routes.googleapis.com/directions/v2:computeRoutes';

  constructor(private readonly configService: ConfigService) {
    // Fail fast if API key not configured
    this.apiKey = this.configService.googleRoutesApiKey;
    this.logger.log('Google Routes API service initialized');
  }

  /**
   * Compute optimized route from origin through waypoints to destination
   *
   * @param origin Starting location
   * @param waypoints List of intermediate stops (1-15 locations)
   * @param destination Return destination
   * @returns Optimized route with distance, duration, and waypoint order
   */
  async computeOptimizedRoute(
    origin: RouteLocation,
    waypoints: RouteLocation[],
    destination: RouteLocation,
  ): Promise<RouteResult> {
    if (waypoints.length === 0) {
      throw new InternalServerErrorException('At least one waypoint is required');
    }

    if (waypoints.length > 15) {
      throw new InternalServerErrorException('Maximum 15 waypoints allowed');
    }

    try {
      this.logger.log(`Computing route: origin -> ${waypoints.length} waypoints -> destination`);

      // Build request payload for Google Routes API
      const requestBody = {
        origin: {
          location: {
            latLng: {
              latitude: origin.lat,
              longitude: origin.lng,
            },
          },
        },
        destination: {
          location: {
            latLng: {
              latitude: destination.lat,
              longitude: destination.lng,
            },
          },
        },
        intermediates: waypoints.map((wp) => ({
          location: {
            latLng: {
              latitude: wp.lat,
              longitude: wp.lng,
            },
          },
        })),
        travelMode: 'DRIVE',
        routingPreference: 'TRAFFIC_AWARE',
        optimizeWaypointOrder: true, // Enable waypoint optimization
        computeAlternativeRoutes: false,
        routeModifiers: {
          avoidTolls: false,
          avoidHighways: false,
          avoidFerries: true,
        },
        languageCode: 'vi',
        units: 'METRIC',
      };

      const response = await axios.post(this.apiUrl, requestBody, {
        headers: {
          'Content-Type': 'application/json',
          'X-Goog-Api-Key': this.apiKey,
          'X-Goog-FieldMask': 'routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.optimizedIntermediateWaypointIndex',
        },
        timeout: 10000, // 10 second timeout
      });

      // Extract route data from response
      const route = response.data.routes?.[0];
      if (!route) {
        throw new InternalServerErrorException('No route found in Google Routes API response');
      }

      // Parse distance and duration
      const distanceMeters = parseInt(route.distanceMeters || '0', 10);
      const durationSeconds = this.parseDuration(route.duration);

      // Get optimized waypoint order (indices)
      const waypointOrder: number[] = route.optimizedIntermediateWaypointIndex || waypoints.map((_, i) => i);

      // Optional: extract polyline
      const polyline = route.polyline?.encodedPolyline;

      this.logger.log(
        `Route computed: ${distanceMeters}m, ${durationSeconds}s, waypoint order: [${waypointOrder.join(',')}]`,
      );

      return {
        distance: distanceMeters,
        duration: durationSeconds,
        polyline,
        waypointOrder,
      };
    } catch (error) {
      this.handleRoutesApiError(error);
    }
  }

  /**
   * Parse duration string (e.g., "1234s") to seconds
   */
  private parseDuration(duration: string | undefined): number {
    if (!duration) return 0;
    const match = duration.match(/(\d+)s/);
    return match ? parseInt(match[1], 10) : 0;
  }

  /**
   * Handle Google Routes API errors
   */
  private handleRoutesApiError(error: unknown): never {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      const status = axiosError.response?.status;
      const errorData = axiosError.response?.data as any;

      this.logger.error('Google Routes API error:', {
        status,
        message: axiosError.message,
        data: errorData,
      });

      // Map common API errors to user-friendly messages
      if (status === 400) {
        throw new InternalServerErrorException('Invalid route request. Please check locations.');
      } else if (status === 401 || status === 403) {
        throw new InternalServerErrorException('Google Routes API authentication failed. Check API key.');
      } else if (status === 429) {
        throw new InternalServerErrorException('Route optimization temporarily unavailable. Please try again.');
      } else {
        throw new InternalServerErrorException('Route optimization failed. Please try again.');
      }
    }

    this.logger.error('Unexpected error in Google Routes API:', error);
    throw new InternalServerErrorException('Route optimization failed due to unexpected error');
  }
}
