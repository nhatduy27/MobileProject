import { Controller, Post, Get, Body, Param, Query, UseGuards, Req } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiQuery, ApiBody } from '@nestjs/swagger';
import { ReviewsService } from '../services/reviews.service';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';
import { CreateReviewDto } from '../dto/create-review.dto';

/**
 * Reviews Controller
 *
 * Endpoints for managing reviews
 */
@ApiTags('Reviews')
@Controller('reviews')
export class ReviewsController {
  constructor(private readonly reviewsService: ReviewsService) {}

  /**
   * POST /reviews
   * Create a review for an order with optional product reviews
   */
  @Post()
  @UseGuards(AuthGuard, RolesGuard)
  @Roles(UserRole.CUSTOMER)
  @ApiBearerAuth('firebase-auth')
  @ApiOperation({ summary: 'Tạo đánh giá cho đơn hàng (có thể đánh giá từng sản phẩm)' })
  @ApiBody({ type: CreateReviewDto })
  @ApiResponse({
    status: 201,
    description: 'Đánh giá thành công',
    schema: {
      example: {
        success: true,
        data: {
          review: {
            id: 'rev_abc',
            orderId: 'ord_123',
            customerId: 'uid_123',
            customerName: 'Nguyễn Văn A',
            shopId: 'shop_456',
            rating: 5,
            comment: 'Đồ ăn ngon, giao hàng nhanh!',
            productReviews: [
              { productId: 'pho_bo_id', productName: 'Phở bò', rating: 5, comment: 'Phở bò rất ngon!' },
              { productId: 'tra_sua_id', productName: 'Trà sữa', rating: 4, comment: 'Hơi ngọt' },
            ],
            createdAt: '2026-01-28T10:00:00.000Z',
          },
          productRatingsUpdated: [
            { productId: 'pho_bo_id', productName: 'Phở bò', newRating: 4.5, totalRatings: 10 },
            { productId: 'tra_sua_id', productName: 'Trà sữa', newRating: 4.2, totalRatings: 8 },
          ],
        },
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Validation error',
    schema: {
      example: {
        success: false,
        message: 'REVIEW_009: Sản phẩm xxx không có trong đơn hàng',
      },
    },
  })
  async createReview(@Req() req: any, @Body() dto: CreateReviewDto) {
    return this.reviewsService.createReview(
      req.user.uid,
      req.user.displayName || 'Khách hàng',
      dto.orderId,
      dto.rating,
      dto.comment,
      dto.productReviews,
    );
  }

  /**
   * GET /reviews/my
   * Get current customer's reviews
   */
  @Get('my')
  @UseGuards(AuthGuard, RolesGuard)
  @Roles(UserRole.CUSTOMER)
  @ApiBearerAuth('firebase-auth')
  @ApiOperation({ summary: 'Lấy danh sách đánh giá của tôi' })
  async getMyReviews(@Req() req: any) {
    return this.reviewsService.getMyReviews(req.user.uid);
  }

  /**
   * GET /reviews/shop/:shopId
   * Get reviews for a shop (public)
   */
  @Get('shop/:shopId')
  @ApiOperation({ summary: 'Lấy đánh giá của shop' })
  @ApiQuery({ name: 'page', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number })
  @ApiResponse({
    status: 200,
    description: 'Danh sách đánh giá',
    schema: {
      example: {
        success: true,
        data: {
          reviews: [
            {
              id: 'rev_abc',
              customerName: 'Nguyễn Văn A',
              rating: 5,
              comment: 'Đồ ăn ngon!',
              ownerReply: 'Cảm ơn bạn đã ủng hộ!',
              createdAt: '2026-01-28T10:00:00.000Z',
            },
          ],
          total: 50,
          avgRating: 4.5,
        },
      },
    },
  })
  async getShopReviews(
    @Param('shopId') shopId: string,
    @Query('page') page?: string,
    @Query('limit') limit?: string,
  ) {
    return this.reviewsService.getShopReviews(shopId, {
      page: page ? Number(page) : 1,
      limit: limit ? Number(limit) : 20,
    });
  }

  /**
   * GET /reviews/product/:productId
   * Get reviews for a specific product (public)
   */
  @Get('product/:productId')
  @ApiOperation({ summary: 'Lấy đánh giá của sản phẩm' })
  @ApiQuery({ name: 'page', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number })
  @ApiResponse({
    status: 200,
    description: 'Danh sách đánh giá sản phẩm',
    schema: {
      example: {
        success: true,
        data: {
          reviews: [
            {
              reviewId: 'rev_abc',
              customerName: 'Nguyễn Văn A',
              rating: 5,
              comment: 'Phở bò rất ngon!',
              createdAt: '2026-01-28T10:00:00.000Z',
            },
          ],
          total: 25,
          avgRating: 4.5,
          totalRatings: 25,
        },
      },
    },
  })
  async getProductReviews(
    @Param('productId') productId: string,
    @Query('page') page?: string,
    @Query('limit') limit?: string,
  ) {
    return this.reviewsService.getProductReviews(productId, {
      page: page ? Number(page) : 1,
      limit: limit ? Number(limit) : 20,
    });
  }

  /**
   * GET /reviews/order/:orderId
   * Get review for a specific order
   */
  @Get('order/:orderId')
  @UseGuards(AuthGuard)
  @ApiBearerAuth('firebase-auth')
  @ApiOperation({ summary: 'Lấy đánh giá của đơn hàng' })
  async getOrderReview(@Param('orderId') orderId: string) {
    return this.reviewsService.getReviewByOrderId(orderId);
  }
}

/**
 * Owner Reviews Controller
 *
 * Endpoints for shop owners to manage reviews
 */
@ApiTags('Reviews (Owner)')
@Controller('owner/reviews')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@ApiBearerAuth('firebase-auth')
export class OwnerReviewsController {
  constructor(private readonly reviewsService: ReviewsService) {}

  /**
   * POST /owner/reviews/:reviewId/reply
   * Reply to a review
   */
  @Post(':reviewId/reply')
  @ApiOperation({ summary: 'Phản hồi đánh giá' })
  @ApiResponse({
    status: 200,
    description: 'Phản hồi thành công',
  })
  async replyToReview(
    @Req() req: any,
    @Param('reviewId') reviewId: string,
    @Body() body: { reply: string },
  ) {
    await this.reviewsService.replyToReview(req.user.uid, reviewId, body.reply);
    return { message: 'Phản hồi thành công' };
  }
}
