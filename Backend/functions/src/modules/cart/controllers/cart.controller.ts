import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Req,
  UseGuards,
  HttpCode,
  Query,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiNoContentResponse,
  ApiBadRequestResponse,
  ApiNotFoundResponse,
  ApiConflictResponse,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
  ApiInternalServerErrorResponse,
  ApiParam,
  ApiQuery,
} from '@nestjs/swagger';
import { CartService } from '../services';
import { AddToCartDto, UpdateCartItemDto, CartGroupsQueryDto } from '../dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Cart Controller
 *
 * Customer cart management endpoints
 * All endpoints require CUSTOMER authentication
 *
 * Base URL: /cart
 *
 * Tasks: CART-001 to CART-006
 */
@ApiTags('Cart')
@Controller('cart')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.CUSTOMER)
export class CartController {
  constructor(private readonly cartService: CartService) {}

  /**
   * GET /cart
   * Get cart items grouped by shop
   *
   * CART-005
   */
  @Get()
  @ApiOperation({ 
    summary: 'Get cart items grouped by shop',
    description: 'Returns cart items grouped by shop. Returns empty groups array if cart is empty or does not exist.'
  })
  @ApiQuery({ name: 'page', required: false, type: Number, description: 'Page number (1-based, default 1)' })
  @ApiQuery({ name: 'limit', required: false, type: Number, description: 'Items per page (default 10, max 50)' })
  @ApiQuery({ 
    name: 'includeAll', 
    required: false, 
    type: Boolean, 
    description: 'Return all groups without pagination. Query string must be exactly "true" (case-insensitive) to enable. Default: false',
    example: false
  })
  @ApiOkResponse({
    description: 'Cart items grouped by shop (or empty cart)',
    schema: {
      oneOf: [
        {
          description: 'Cart with items (default pagination)',
          example: {
            success: true,
            data: {
              groups: [
                {
                  shopId: 'shop_123',
                  shopName: 'Quán Phở Việt',
                  isOpen: true,
                  shipFee: 0,
                  items: [
                    {
                      productId: 'prod_abc',
                      shopId: 'shop_123',
                      productName: 'Cơm sườn nướng',
                      productImage: 'https://...',
                      quantity: 2,
                      price: 35000,
                      subtotal: 70000,
                      addedAt: '2026-01-18T08:00:00.000Z',
                      updatedAt: '2026-01-18T10:30:00.000Z',
                    },
                  ],
                  subtotal: 70000,
                  lastActivityAt: '2026-01-18T10:30:00.000Z',
                },
              ],
              page: 1,
              limit: 10,
              totalGroups: 3,
              totalPages: 1,
            },
            timestamp: '2026-01-18T10:30:00.000Z',
          },
        },
        {
          description: 'Cart with includeAll=true (all groups, metadata shows totals)',
          example: {
            success: true,
            data: {
              groups: [
                {
                  shopId: 'shop_123',
                  shopName: 'Quán Phở Việt',
                  isOpen: true,
                  shipFee: 0,
                  items: [
                    {
                      productId: 'prod_abc',
                      shopId: 'shop_123',
                      productName: 'Cơm sườn nướng',
                      productImage: 'https://...',
                      quantity: 2,
                      price: 35000,
                      subtotal: 70000,
                      addedAt: '2026-01-18T08:00:00.000Z',
                      updatedAt: '2026-01-18T10:30:00.000Z',
                    },
                  ],
                  subtotal: 70000,
                  lastActivityAt: '2026-01-18T10:30:00.000Z',
                },
              ],
              page: 1,
              limit: 3,
              totalGroups: 3,
              totalPages: 1,
            },
            timestamp: '2026-01-18T10:30:00.000Z',
          },
        },
        {
          description: 'Empty cart',
          example: {
            success: true,
            data: {
              groups: [],
              page: 1,
              limit: 10,
              totalGroups: 0,
              totalPages: 0,
            },
            timestamp: '2026-01-18T10:30:00.000Z',
          },
        },
      ],
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async getCart(@Req() req: any, @Query() query: CartGroupsQueryDto) {
    // Debug logging (enable with DEBUG_CART_QUERY=true)
    if (process.env.DEBUG_CART_QUERY === 'true') {
      console.log('[CART DEBUG] Raw query:', req.query);
      console.log('[CART DEBUG] Parsed DTO:', {
        page: query.page,
        limit: query.limit,
        includeAll: query.includeAll,
        pageType: typeof query.page,
        limitType: typeof query.limit,
        includeAllType: typeof query.includeAll,
      });
    }

    // Robust manual parsing to ensure correct types at runtime
    // This handles edge cases where DTO transformation might not apply correctly
    const includeAllRaw = req.query.includeAll as string | undefined;
    const pageRaw = req.query.page as string | undefined;
    const limitRaw = req.query.limit as string | undefined;

    // Parse includeAll: only string "true" (case-insensitive) enables it
    const includeAll = includeAllRaw?.toLowerCase() === 'true';

    // Parse page: default 1, minimum 1
    const pageNum = pageRaw ? parseInt(pageRaw, 10) : 1;
    const page = Number.isFinite(pageNum) && pageNum >= 1 ? pageNum : 1;

    // Parse limit: default 10, range [1, 50]
    const limitNum = limitRaw ? parseInt(limitRaw, 10) : 10;
    const limit = Number.isFinite(limitNum) 
      ? Math.max(1, Math.min(50, limitNum))
      : 10;

    if (process.env.DEBUG_CART_QUERY === 'true') {
      console.log('[CART DEBUG] Parsed values:', { page, limit, includeAll });
    }

    return this.cartService.getCartGrouped(req.user.uid, {
      includeAll,
      page,
      limit,
    });
  }

  /**
   * GET /cart/shops/:shopId
   * Get cart group detail by shop
   *
   * New endpoint
   */
  @Get('shops/:shopId')
  @ApiOperation({
    summary: 'Get cart group detail by shop',
    description:
      'Fetches a single cart group for the specified shop. Returns { group: null } if there are no items for that shop.',
  })
  @ApiParam({
    name: 'shopId',
    required: true,
    description: 'Shop ID to fetch cart group for',
    example: 'shop_123',
  })
  @ApiOkResponse({
    description: 'Cart group detail for a specific shop',
    schema: {
      oneOf: [
        {
          description: 'Group found',
          example: {
            success: true,
            data: {
              group: {
                shopId: 'shop_123',
                shopName: 'Quán Phở Việt',
                isOpen: true,
                shipFee: 0,
                items: [
                  {
                    productId: 'prod_abc',
                    shopId: 'shop_123',
                    productName: 'Cơm sườn nướng',
                    productImage: 'https://...',
                    quantity: 2,
                    price: 35000,
                    subtotal: 70000,
                    addedAt: '2026-01-18T08:00:00.000Z',
                    updatedAt: '2026-01-18T10:30:00.000Z',
                  },
                ],
                subtotal: 70000,
                lastActivityAt: '2026-01-18T10:30:00.000Z',
              },
            },
            timestamp: '2026-01-18T10:30:00.000Z',
          },
        },
        {
          description: 'Group not found (no items for shop)',
          example: {
            success: true,
            data: {
              group: null,
            },
            timestamp: '2026-01-18T10:30:00.000Z',
          },
        },
      ],
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async getShopGroup(@Req() req: any, @Param('shopId') shopId: string) {
    return this.cartService.getCartGroupByShop(req.user.uid, shopId);
  }

  /**
   * POST /cart/items
   * Add product to cart
   *
   * CART-002
   */
  @Post('items')
  @ApiOperation({ 
    summary: 'Add product to cart',
    description: 'Adds a product to the cart. If product already exists, quantity is incremented. Returns cart id and grouped items.'
  })
  @ApiCreatedResponse({
    description: 'Product added to cart successfully',
    schema: {
      example: {
        success: true,
        data: {
          id: 'cart_user_123',
          groups: [
            {
              shopId: 'shop_123',
              shopName: 'Quán Phở Việt',
              isOpen: true,
              shipFee: 0,
              items: [
                {
                  productId: 'prod_abc',
                  shopId: 'shop_123',
                  productName: 'Cơm sườn nướng',
                  productImage: 'https://...',
                  quantity: 2,
                  price: 35000,
                  subtotal: 70000,
                  addedAt: '2026-01-18T08:00:00.000Z',
                  updatedAt: '2026-01-18T10:30:00.000Z',
                },
              ],
              subtotal: 70000,
              lastActivityAt: '2026-01-18T10:30:00.000Z',
            },
          ],
        },
        timestamp: '2026-01-18T10:30:00.000Z',
      },
    },
  })
  @ApiBadRequestResponse({
    description: 'Invalid request body - validation failed',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_400',
        message: 'Validation failed: quantity must be an integer between 1 and 999',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiNotFoundResponse({
    description: 'Product not found or unavailable',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_001',
        message: 'Product not found or unavailable',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiConflictResponse({
    description: 'Shop is currently closed',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_002',
        message: 'Shop is currently closed',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async addToCart(@Req() req: any, @Body() dto: AddToCartDto) {
    return this.cartService.addToCart(req.user.uid, dto);
  }

  /**
   * PUT /cart/items/:productId
   * Update cart item quantity
   *
   * CART-003
   */
  @Put('items/:productId')
  @ApiOperation({ 
    summary: 'Update cart item quantity',
    description: 'Updates the quantity of a specific product in the cart. Returns updated cart with grouped items.'
  })
  @ApiParam({ 
    name: 'productId', 
    description: 'Product ID to update',
    example: 'prod_abc123'
  })
  @ApiOkResponse({
    description: 'Cart item quantity updated successfully',
    schema: {
      example: {
        success: true,
        data: {
          id: 'cart_user_123',
          groups: [
            {
              shopId: 'shop_123',
              shopName: 'Quán Phở Việt',
              isOpen: true,
              shipFee: 0,
              items: [
                {
                  productId: 'prod_abc',
                  shopId: 'shop_123',
                  productName: 'Cơm sườn nướng',
                  productImage: 'https://...',
                  quantity: 3,
                  price: 35000,
                  subtotal: 105000,
                  addedAt: '2026-01-18T08:00:00.000Z',
                  updatedAt: '2026-01-18T10:35:00.000Z',
                },
              ],
              subtotal: 105000,
              lastActivityAt: '2026-01-18T10:35:00.000Z',
            },
          ],
        },
        timestamp: '2026-01-18T10:35:00.000Z',
      },
    },
  })
  @ApiBadRequestResponse({
    description: 'Invalid request body - validation failed',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_400',
        message: 'Validation failed: quantity must be an integer between 1 and 999',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiNotFoundResponse({
    description: 'Cart or product not found in cart',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_003',
        message: 'Product not found in cart',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async updateCartItem(
    @Req() req: any,
    @Param('productId') productId: string,
    @Body() dto: UpdateCartItemDto,
  ) {
    return this.cartService.updateCartItem(req.user.uid, productId, dto);
  }

  /**
   * DELETE /cart/items/:productId
   * Remove item from cart
   *
   * CART-004
   */
  @Delete('items/:productId')
  @HttpCode(204)
  @ApiOperation({ 
    summary: 'Remove item from cart',
    description: 'Removes a specific product from the cart. If cart becomes empty after removal, the cart document is deleted.'
  })
  @ApiParam({ 
    name: 'productId', 
    required: true,
    description: 'Product ID to remove from cart',
    example: 'prod_abc123'
  })
  @ApiNoContentResponse({ description: 'Item removed from cart successfully (no response body)' })
  @ApiNotFoundResponse({
    description: 'Product not found in cart',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_004',
        message: 'Product not found in cart',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async removeCartItem(@Req() req: any, @Param('productId') productId: string) {
    await this.cartService.removeCartItem(req.user.uid, productId);
  }

  /**
   * DELETE /cart
   * Clear entire cart
   *
   * CART-006
   *
   * This endpoint is idempotent - calling it multiple times
   * returns 204 No Content even if the cart does not exist.
   */
  @Delete()
  @HttpCode(204)
  @ApiOperation({ 
    summary: 'Clear entire cart',
    description: 'Clears all items from the cart. This operation is idempotent - returns 204 even if cart is already empty or does not exist.'
  })
  @ApiNoContentResponse({ description: 'Cart cleared successfully (no response body)' })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async clearCart(@Req() req: any) {
    await this.cartService.clearCart(req.user.uid);
  }


  /**
   * DELETE /cart/shops/:shopId
   * Clear cart items for a specific shop
   *
   * CART-007
   */
  @Delete('shops/:shopId')
  @ApiOperation({ 
    summary: 'Clear cart items for a shop',
    description: 'Removes all items belonging to a specific shop from the cart. Returns updated cart grouped by shop. If cart becomes empty, returns empty groups array.'
  })
  @ApiParam({ 
    name: 'shopId', 
    required: true,
    description: 'Shop ID to clear items for',
    example: 'shop_123'
  })
  @ApiOkResponse({
    description: 'Shop items cleared successfully',
    schema: {
      oneOf: [
        {
          description: 'Items removed, cart still has other shops',
          example: {
            success: true,
            data: {
              removedCount: 3,
              groups: [
                {
                  shopId: 'shop_456',
                  shopName: 'Other Shop',
                  isOpen: true,
                  shipFee: 0,
                  items: [
                    {
                      productId: 'prod_xyz',
                      shopId: 'shop_456',
                      productName: 'Other Product',
                      productImage: 'https://...',
                      quantity: 2,
                      price: 30000,
                      subtotal: 60000,
                    },
                  ],
                  subtotal: 60000,
                },
              ],
            },
          },
        },
        {
          description: 'Cart became empty after removal',
          example: {
            success: true,
            data: {
              removedCount: 2,
              groups: [],
            },
          },
        },
        {
          description: 'Shop not found in cart',
          example: {
            success: true,
            data: {
              removedCount: 0,
              groups: [
                {
                  shopId: 'shop_789',
                  shopName: 'Existing Shop',
                  isOpen: true,
                  shipFee: 0,
                  items: [
                    {
                      productId: 'prod_abc',
                      quantity: 1,
                      price: 50000,
                      subtotal: 50000,
                    },
                  ],
                  subtotal: 50000,
                },
              ],
            },
          },
        },
      ],
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Invalid or missing authentication token',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_401',
        message: 'Unauthorized',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have CUSTOMER role',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_403',
        message: 'Forbidden - CUSTOMER role required',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  @ApiInternalServerErrorResponse({
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        errorCode: 'CART_500',
        message: 'Internal server error',
        timestamp: '2026-01-18T00:00:00.000Z',
      },
    },
  })
  async clearShopGroup(@Req() req: any, @Param('shopId') shopId: string) {
    return this.cartService.clearCartByShop(req.user.uid, shopId);
  }
}
