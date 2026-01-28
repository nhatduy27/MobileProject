import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  BadRequestException,
  ForbiddenException,
} from '@nestjs/common';
import { FirestoreReviewsRepository } from '../repositories/firestore-reviews.repository';
import { ReviewEntity } from '../entities/review.entity';
import { OrdersService } from './orders.service';
import { ShopsService } from '../../shops/services/shops.service';

export const REVIEWS_REPOSITORY = 'REVIEWS_REPOSITORY';

@Injectable()
export class ReviewsService {
  constructor(
    @Inject(REVIEWS_REPOSITORY)
    private readonly reviewsRepository: FirestoreReviewsRepository,
    private readonly ordersService: OrdersService,
    private readonly shopsService: ShopsService,
  ) {}

  /**
   * Create a review for an order
   * Validation:
   * - Order must exist and be DELIVERED
   * - Customer must own the order
   * - Order must not already have a review
   */
  async createReview(
    customerId: string,
    customerName: string,
    orderId: string,
    rating: number,
    comment?: string,
  ): Promise<ReviewEntity> {
    // Validate rating
    if (rating < 1 || rating > 5) {
      throw new BadRequestException({
        code: 'REVIEW_001',
        message: 'Rating phải từ 1 đến 5',
        statusCode: 400,
      });
    }

    // Get order and validate
    const order = await this.ordersService.getOrderDetail(customerId, orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'REVIEW_002',
        message: 'Không tìm thấy đơn hàng',
        statusCode: 404,
      });
    }

    // Check order ownership
    if (order.customerId !== customerId) {
      throw new ForbiddenException({
        code: 'REVIEW_003',
        message: 'Bạn không phải chủ đơn hàng',
        statusCode: 403,
      });
    }

    // Check order status
    if (order.status !== 'DELIVERED') {
      throw new BadRequestException({
        code: 'REVIEW_004',
        message: 'Chỉ có thể đánh giá đơn hàng đã giao thành công',
        statusCode: 400,
      });
    }

    // Check if review already exists
    const existingReview = await this.reviewsRepository.findByOrderId(orderId);
    if (existingReview) {
      throw new ConflictException({
        code: 'REVIEW_005',
        message: 'Bạn đã đánh giá đơn hàng này rồi',
        statusCode: 409,
      });
    }

    

    // Create review
    const review = await this.reviewsRepository.create({
      orderId,
      customerId,
      customerName,
      shopId: order.shopId,
      rating,
      comment,
    });

    // Update shop stats
    await this.updateShopStats(order.shopId);

    return review;
  }

  /**
   * Get reviews for a shop (public)
   */
  async getShopReviews(
    shopId: string,
    options: { page?: number; limit?: number } = {},
  ): Promise<{ reviews: ReviewEntity[]; total: number; avgRating: number }> {
    const { reviews, total } = await this.reviewsRepository.findByShopId(shopId, options);
    const stats = await this.reviewsRepository.getShopStats(shopId);

    return {
      reviews,
      total,
      avgRating: stats.avgRating,
    };
  }

  /**
   * Get review by order ID
   */
  async getReviewByOrderId(orderId: string): Promise<ReviewEntity | null> {
    return this.reviewsRepository.findByOrderId(orderId);
  }

  /**
   * Get customer's reviews
   */
  async getMyReviews(customerId: string): Promise<ReviewEntity[]> {
    return this.reviewsRepository.findByCustomerId(customerId);
  }

  /**
   * Owner reply to a review
   */
  async replyToReview(ownerId: string, reviewId: string, reply: string): Promise<void> {
    const review = await this.reviewsRepository.findById(reviewId);
    if (!review) {
      throw new NotFoundException({
        code: 'REVIEW_006',
        message: 'Không tìm thấy đánh giá',
        statusCode: 404,
      });
    }

    // Verify shop ownership
    await this.shopsService.verifyShopOwnership(review.shopId, ownerId);

    // Check if already replied
    if (review.ownerReply) {
      throw new ConflictException({
        code: 'REVIEW_007',
        message: 'Bạn đã phản hồi đánh giá này rồi',
        statusCode: 409,
      });
    }

    await this.reviewsRepository.update(reviewId, {
      ownerReply: reply,
      ownerRepliedAt: new Date().toISOString(),
    });
  }

  /**
   * Update shop rating stats after review
   */
  private async updateShopStats(shopId: string): Promise<void> {
    const stats = await this.reviewsRepository.getShopStats(shopId);
    await this.shopsService.updateShopStats(shopId, {
      rating: stats.avgRating,
      totalRatings: stats.totalReviews,
    });
  }
}
