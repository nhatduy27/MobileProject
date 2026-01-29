import {
  Controller,
  Post,
  Get,
  Body,
  Query,
  UseGuards,
  Req,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { GpsService } from '../services/gps.service';
import { CreateOptimizedTripDto } from '../dto/create-optimized-trip.dto';
import {
  GetMyTripDto,
  ListMyTripsDto,
  StartTripDto,
  FinishTripDto,
  CancelTripDto,
} from '../dto/trip-query.dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators';
import { UserRole, AuthenticatedRequest } from '../../../core/interfaces/user.interface';

/**
 * GPS Controller
 *
 * Handles shipper route optimization and trip management endpoints.
 */
@ApiTags('GPS - Shipper Routing')
@Controller('gps')
@UseGuards(AuthGuard, RolesGuard)
@ApiBearerAuth('firebase-auth')
export class GpsController {
  constructor(private readonly gpsService: GpsService) {}

  /**
   * Create optimized trip for shipper
   *
   * Callable: POST /api/gps/create-optimized-trip
   * Auth: SHIPPER only
   */
  @Post('create-optimized-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Create optimized delivery trip',
    description:
      'Creates a new trip with optimized route using Google Routes API. ' +
      'Takes 1-15 order IDs, extracts buildings, and computes optimal route.',
  })
  @ApiResponse({
    status: 201,
    description: 'Trip created successfully with optimized route',
    schema: {
      example: {
        success: true,
        data: {
          id: 'trip_abc123',
          shipperId: 'user_shipper_123',
          status: 'PENDING',
          origin: { lat: 10.773589, lng: 106.659924, name: 'Cổng chính KTX' },
          returnTo: { lat: 10.773589, lng: 106.659924, name: 'Cổng chính KTX' },
          waypoints: [
            {
              buildingCode: 'A1',
              location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
              order: 1,
            },
            {
              buildingCode: 'B2',
              location: { lat: 10.88215, lng: 106.7814, name: 'Tòa B2' },
              order: 2,
            },
          ],
          orders: [
            {
              orderId: 'order_001',
              buildingCode: 'A1',
              tripDeliveryStatus: 'NOT_VISITED',
              stopIndex: 1,
            },
            {
              orderId: 'order_002',
              buildingCode: 'B2',
              tripDeliveryStatus: 'NOT_VISITED',
              stopIndex: 2,
            },
          ],
          route: {
            distance: 2450,
            duration: 420,
            polyline: 'u~pvFwxdlSabC~aB_fDwqE',
            waypointOrder: [0, 1],
          },
          totalDistance: 2450,
          totalDuration: 420,
          totalOrders: 2,
          totalBuildings: 2,
          createdAt: '2026-01-29T10:00:00.000Z',
          updatedAt: '2026-01-29T10:00:00.000Z',
        },
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid request (e.g., too many orders, missing buildings, invalid order status)',
    schema: {
      example: {
        success: false,
        message: 'Delivery point not found for building: X9',
        errorCode: 'BAD_REQUEST',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 401,
    description: 'Unauthorized - Not authenticated',
    schema: {
      example: {
        success: false,
        message: 'Unauthorized',
        errorCode: 'UNAUTHORIZED',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Not a shipper',
    schema: {
      example: {
        success: false,
        message: 'Forbidden',
        errorCode: 'FORBIDDEN',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Order not found',
    schema: {
      example: {
        success: false,
        message: 'Order order_xyz not found or not assigned to you',
        errorCode: 'NOT_FOUND',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 500,
    description: 'Internal server error (e.g., Google Routes API failure)',
    schema: {
      example: {
        success: false,
        message: 'Internal server error',
        errorCode: 'INTERNAL_ERROR',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  async createOptimizedTrip(@Req() req: AuthenticatedRequest, @Body() dto: CreateOptimizedTripDto) {
    const shipperId = req.user.uid;

    const trip = await this.gpsService.createOptimizedTrip(shipperId, dto);

    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return trip;
  }

  /**
   * Get single trip by ID
   *
   * Callable: GET /api/gps/trip?tripId=...
   * Auth: SHIPPER only
   */
  @Get('trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Get trip by ID',
    description: 'Retrieves a single trip. Shipper must own the trip.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip retrieved successfully',
    schema: {
      example: {
        success: true,
        data: {
          id: 'trip_abc123',
          shipperId: 'user_shipper_123',
          status: 'PENDING',
          origin: { lat: 10.773589, lng: 106.659924, name: 'Cổng chính KTX' },
          returnTo: { lat: 10.773589, lng: 106.659924, name: 'Cổng chính KTX' },
          waypoints: [
            {
              buildingCode: 'A1',
              location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
              order: 1,
            },
          ],
          orders: [
            {
              orderId: 'order_001',
              buildingCode: 'A1',
              tripDeliveryStatus: 'NOT_VISITED',
              stopIndex: 1,
            },
          ],
          route: {
            distance: 1200,
            duration: 180,
            polyline: 'u~pvFwxdlS',
            waypointOrder: [0],
          },
          totalDistance: 1200,
          totalDuration: 180,
          totalOrders: 1,
          totalBuildings: 1,
          createdAt: '2026-01-29T09:00:00.000Z',
          updatedAt: '2026-01-29T09:30:00.000Z',
        },
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Trip belongs to another shipper',
    schema: {
      example: {
        success: false,
        message: 'You do not have permission to view this trip',
        errorCode: 'FORBIDDEN',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found',
    schema: {
      example: {
        success: false,
        message: 'Trip not found',
        errorCode: 'NOT_FOUND',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  async getMyTrip(@Req() req: AuthenticatedRequest, @Query() dto: GetMyTripDto) {
    const shipperId = req.user.uid;
    const trip = await this.gpsService.getMyTrip(shipperId, dto.tripId);

    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return trip;
  }

  /**
   * List trips with pagination
   *
   * Callable: GET /api/gps/trips?status=PENDING&page=1&limit=20
   * Auth: SHIPPER only
   */
  @Get('trips')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'List my trips with pagination',
    description: 'Returns paginated list of trips for current shipper. Supports status filter.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trips retrieved successfully',
    schema: {
      example: {
        success: true,
        data: {
          items: [
            {
              id: 'trip_001',
              shipperId: 'user_shipper_123',
              status: 'PENDING',
              origin: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
              returnTo: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
              waypoints: [
                {
                  buildingCode: 'A1',
                  location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
                  order: 1,
                },
              ],
              orders: [
                {
                  orderId: 'order_001',
                  buildingCode: 'A1',
                  tripDeliveryStatus: 'NOT_VISITED',
                  stopIndex: 1,
                },
              ],
              route: { distance: 1200, duration: 180, polyline: 'u~pvFwxdlS', waypointOrder: [0] },
              totalDistance: 1200,
              totalDuration: 180,
              totalOrders: 1,
              totalBuildings: 1,
              createdAt: '2026-01-29T09:00:00.000Z',
              updatedAt: '2026-01-29T09:00:00.000Z',
            },
            {
              id: 'trip_002',
              shipperId: 'user_shipper_123',
              status: 'STARTED',
              origin: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
              returnTo: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
              waypoints: [
                {
                  buildingCode: 'B2',
                  location: { lat: 10.88215, lng: 106.7814, name: 'Tòa B2' },
                  order: 1,
                },
              ],
              orders: [
                {
                  orderId: 'order_002',
                  buildingCode: 'B2',
                  tripDeliveryStatus: 'NOT_VISITED',
                  stopIndex: 1,
                },
              ],
              route: { distance: 1500, duration: 200, polyline: 'abC~aB', waypointOrder: [0] },
              totalDistance: 1500,
              totalDuration: 200,
              totalOrders: 1,
              totalBuildings: 1,
              createdAt: '2026-01-29T08:00:00.000Z',
              updatedAt: '2026-01-29T08:30:00.000Z',
              startedAt: '2026-01-29T08:30:00.000Z',
            },
          ],
          page: 1,
          limit: 20,
          total: 42,
          totalPages: 3,
          hasNext: true,
        },
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 401,
    description: 'Unauthorized - Not authenticated',
    schema: {
      example: {
        success: false,
        message: 'Unauthorized',
        errorCode: 'UNAUTHORIZED',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  async listMyTrips(@Req() req: AuthenticatedRequest, @Query() dto: ListMyTripsDto) {
    const shipperId = req.user.uid;
    const result = await this.gpsService.listMyTrips(shipperId, dto.status, dto.page, dto.limit);

    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return result;
  }

  /**
   * Start a trip (PENDING -> STARTED)
   *
   * Callable: POST /api/gps/start-trip
   * Auth: SHIPPER only
   */
  @Post('start-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Start trip',
    description:
      'Changes trip status from PENDING to STARTED. Sets startedAt timestamp. ' +
      'Also updates all trip orders from READY → SHIPPING status atomically.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip started successfully. All orders updated to SHIPPING status.',
    schema: {
      example: {
        success: true,
        data: {
          id: 'trip_abc123',
          shipperId: 'user_shipper_123',
          status: 'STARTED',
          origin: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
          returnTo: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
          waypoints: [
            {
              buildingCode: 'A1',
              location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
              order: 1,
            },
          ],
          orders: [
            {
              orderId: 'order_001',
              buildingCode: 'A1',
              tripDeliveryStatus: 'NOT_VISITED',
              stopIndex: 1,
            },
          ],
          route: { distance: 1200, duration: 180, polyline: 'u~pvFwxdlS', waypointOrder: [0] },
          totalDistance: 1200,
          totalDuration: 180,
          totalOrders: 1,
          totalBuildings: 1,
          createdAt: '2026-01-29T09:00:00.000Z',
          updatedAt: '2026-01-29T10:00:00.000Z',
          startedAt: '2026-01-29T10:00:00.000Z',
        },
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found or order not found',
    schema: {
      example: {
        success: false,
        message: 'Trip not found',
        errorCode: 'NOT_FOUND',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - One or more orders are not assigned to this shipper',
    schema: {
      example: {
        success: false,
        message:
          'Order order_001 is not assigned to shipper user_shipper_123. Current shipper: user_other_shipper',
        errorCode: 'FORBIDDEN',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 409,
    description:
      'Conflict - Trip is not in PENDING status OR one or more orders are not in READY status',
    schema: {
      example: {
        success: false,
        message: 'Cannot start trip. Trip is STARTED. Only PENDING trips can be started.',
        errorCode: 'CONFLICT',
        timestamp: '2026-01-29T10:00:00.123Z',
      },
    },
  })
  async startTrip(@Req() req: AuthenticatedRequest, @Body() dto: StartTripDto) {
    const shipperId = req.user.uid;
    const trip = await this.gpsService.startTrip(shipperId, dto.tripId);

    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return trip;
  }

  /**
   * Finish a trip (STARTED -> FINISHED)
   *
   * Callable: POST /api/gps/finish-trip
   * Auth: SHIPPER only
   */
  @Post('finish-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Finish trip',
    description:
      'Changes trip status from STARTED to FINISHED. Sets finishedAt timestamp. ' +
      'Also updates all trip orders from SHIPPING → DELIVERED status atomically.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip finished successfully. All orders updated to DELIVERED status.',
    schema: {
      example: {
        success: true,
        data: {
          trip: {
            id: 'trip_abc123',
            shipperId: 'user_shipper_123',
            status: 'FINISHED',
            origin: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
            returnTo: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
            waypoints: [
              {
                buildingCode: 'A1',
                location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
                order: 1,
              },
            ],
            orders: [
              {
                orderId: 'order_001',
                buildingCode: 'A1',
                tripDeliveryStatus: 'VISITED',
                stopIndex: 1,
              },
            ],
            route: { distance: 1200, duration: 180, polyline: 'u~pvFwxdlS', waypointOrder: [0] },
            totalDistance: 1200,
            totalDuration: 180,
            totalOrders: 1,
            totalBuildings: 1,
            createdAt: '2026-01-29T09:00:00.000Z',
            updatedAt: '2026-01-29T11:00:00.000Z',
            startedAt: '2026-01-29T10:00:00.000Z',
            finishedAt: '2026-01-29T11:00:00.000Z',
          },
          ordersDelivered: 1,
        },
        timestamp: '2026-01-29T11:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found or order not found',
    schema: {
      example: {
        success: false,
        message: 'Order order_001 not found',
        errorCode: 'NOT_FOUND',
        timestamp: '2026-01-29T11:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - One or more orders are not assigned to this shipper',
    schema: {
      example: {
        success: false,
        message:
          'Order order_001 is not assigned to shipper user_shipper_123. Current shipper: none',
        errorCode: 'FORBIDDEN',
        timestamp: '2026-01-29T11:00:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 409,
    description:
      'Conflict - Trip is not in STARTED status OR one or more orders are not in SHIPPING status',
    schema: {
      example: {
        success: false,
        message: 'Cannot finish trip. Trip is PENDING. Only STARTED trips can be finished.',
        errorCode: 'CONFLICT',
        timestamp: '2026-01-29T11:00:00.123Z',
      },
    },
  })
  async finishTrip(@Req() req: AuthenticatedRequest, @Body() dto: FinishTripDto) {
    const shipperId = req.user.uid;
    const result = await this.gpsService.finishTrip(shipperId, dto.tripId);

    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return result;
  }

  /**
   * Cancel a trip (PENDING -> CANCELLED)
   *
   * Callable: POST /api/gps/cancel-trip
   * Auth: SHIPPER only
   */
  @Post('cancel-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Cancel trip',
    description:
      'Cancels a PENDING trip. Sets status to CANCELLED and records cancelledAt timestamp. ' +
      'Only PENDING trips can be cancelled. Does NOT update Orders collection status.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip cancelled successfully',
    schema: {
      example: {
        success: true,
        data: {
          id: 'trip_abc123',
          shipperId: 'user_shipper_123',
          status: 'CANCELLED',
          origin: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
          returnTo: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
          waypoints: [
            {
              buildingCode: 'A1',
              location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
              order: 1,
            },
          ],
          orders: [
            {
              orderId: 'order_001',
              buildingCode: 'A1',
              tripDeliveryStatus: 'NOT_VISITED',
              stopIndex: 1,
            },
          ],
          route: { distance: 1200, duration: 180, polyline: 'u~pvFwxdlS', waypointOrder: [0] },
          totalDistance: 1200,
          totalDuration: 180,
          totalOrders: 1,
          totalBuildings: 1,
          createdAt: '2026-01-29T09:00:00.000Z',
          updatedAt: '2026-01-29T10:30:00.000Z',
          cancelledAt: '2026-01-29T10:30:00.000Z',
          cancelReason: 'Customer requested cancellation',
        },
        timestamp: '2026-01-29T10:30:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found',
    schema: {
      example: {
        success: false,
        message: 'Trip not found',
        errorCode: 'NOT_FOUND',
        timestamp: '2026-01-29T10:30:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Trip belongs to another shipper',
    schema: {
      example: {
        success: false,
        message: 'You do not have permission to view this trip',
        errorCode: 'FORBIDDEN',
        timestamp: '2026-01-29T10:30:00.123Z',
      },
    },
  })
  @ApiResponse({
    status: 409,
    description: 'Conflict - Trip is not in PENDING status. Only PENDING trips can be cancelled.',
    schema: {
      example: {
        success: false,
        message: 'Cannot cancel trip. Trip is STARTED. Only PENDING trips can be cancelled.',
        errorCode: 'CONFLICT',
        timestamp: '2026-01-29T10:30:00.123Z',
      },
    },
  })
  async cancelTrip(@Req() req: AuthenticatedRequest, @Body() dto: CancelTripDto) {
    const shipperId = req.user.uid;
    const trip = await this.gpsService.cancelTrip(shipperId, dto.tripId, dto.reason);

    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return trip;
  }

  /**
   * Get all active KTX delivery points
   *
   * @deprecated Use GET /api/delivery-points instead
   * Endpoint này sẽ bị xóa trong phiên bản tương lai.
   * Vui lòng sử dụng /api/delivery-points cho endpoint chính thức.
   *
   * Callable: GET /api/gps/delivery-points
   * Auth: CUSTOMER, SHIPPER, OWNER, ADMIN
   */
  @Get('delivery-points')
  @Roles(UserRole.CUSTOMER, UserRole.SHIPPER, UserRole.OWNER, UserRole.ADMIN)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: '[DEPRECATED] List active KTX delivery points',
    description:
      '⚠️ DEPRECATED: Sử dụng GET /api/delivery-points thay thế. ' +
      'Endpoint này chỉ giữ lại để backward compatibility và sẽ bị xóa sau. ' +
      'Returns all active delivery points (KTX buildings) sorted by block (A-E) and number. ' +
      'Only returns buildings where active=true.',
    deprecated: true,
  })
  @ApiResponse({
    status: 200,
    description: 'Delivery points retrieved successfully',
    schema: {
      example: {
        success: true,
        data: [
          {
            id: 'A1',
            buildingCode: 'A1',
            name: 'Tòa A1',
            location: {
              lat: 10.881765,
              lng: 106.781719,
            },
            note: 'Khu A - Tòa A1',
            active: true,
            createdAt: '2024-01-28T12:00:00.000Z',
            updatedAt: '2024-01-28T12:00:00.000Z',
          },
        ],
        timestamp: '2026-01-28T12:00:00.000Z',
      },
    },
  })
  @ApiResponse({
    status: 401,
    description: 'Unauthorized - Not authenticated',
  })
  async getDeliveryPoints() {
    // Proxy to shared DeliveryPointsService via GpsService
    // TODO: Remove this endpoint after frontend migration to /api/delivery-points
    // Return RAW data - TransformInterceptor wraps with {success, data, timestamp}
    return this.gpsService.listActiveDeliveryPoints();
  }
}
