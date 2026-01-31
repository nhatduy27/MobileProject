import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  BadRequestException,
  ForbiddenException,
  Logger,
} from '@nestjs/common';
import { FirestoreReviewsRepository } from '../repositories/firestore-reviews.repository';
import { ReviewEntity, ProductReview } from '../entities/review.entity';
import { OrdersService } from './orders.service';
import { ShopsService } from '../../shops/services/shops.service';
import { ProductsService } from '../../products/services/products.service';
import { ProductReviewDto } from '../dto/create-review.dto';

export const REVIEWS_REPOSITORY = 'REVIEWS_REPOSITORY';

/**
 * Response type for product rating updates
 */
export interface ProductRatingUpdate {
  productId: string;
  productName: string;
  newRating: number;
  totalRatings: number;
}

@Injectable()
export class ReviewsService {
  private readonly logger = new Logger(ReviewsService.name);

  constructor(
    @Inject(REVIEWS_REPOSITORY)
    private readonly reviewsRepository: FirestoreReviewsRepository,
    private readonly ordersService: OrdersService,
    private readonly shopsService: ShopsService,
    private readonly productsService: ProductsService,
  ) {}

  /**
   * Create a review for an order
   * Supports both shop-level rating and individual product ratings
   * 
   * Validation:
   * - Order must exist and be DELIVERED
   * - Customer must own the order
   * - Order must not already have a review
   * - Product IDs in productReviews must match order items
   * 
   * @returns Review entity and product rating updates
   */
  async createReview(
    customerId: string,
    customerName: string,
    orderId: string,
    rating: number,
    comment?: string,
    productReviews?: ProductReviewDto[],
  ): Promise<{
    review: ReviewEntity;
    productRatingsUpdated?: ProductRatingUpdate[];
  }> {
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

    // Build order product ID set for validation
    const orderProductIds = new Set(order.items.map((item) => item.productId));
    const orderProductNameMap = new Map(
      order.items.map((item) => [item.productId, item.productName]),
    );

    // Validate and enrich product reviews
    let enrichedProductReviews: ProductReview[] | undefined;
    if (productReviews && productReviews.length > 0) {
      // Check for duplicate productIds in productReviews
      const reviewProductIds = productReviews.map((pr) => pr.productId);
      const uniqueProductIds = new Set(reviewProductIds);
      if (reviewProductIds.length !== uniqueProductIds.size) {
        throw new BadRequestException({
          code: 'REVIEW_008',
          message: 'Không thể đánh giá cùng một sản phẩm nhiều lần trong một review',
          statusCode: 400,
        });
      }

      // Validate each productId exists in order items
      for (const pr of productReviews) {
        if (!orderProductIds.has(pr.productId)) {
          throw new BadRequestException({
            code: 'REVIEW_009',
            message: `Sản phẩm ${pr.productId} không có trong đơn hàng`,
            statusCode: 400,
          });
        }
      }

      // Enrich with product names
      enrichedProductReviews = productReviews.map((pr) => ({
        productId: pr.productId,
        productName: orderProductNameMap.get(pr.productId) || 'Unknown',
        rating: pr.rating,
        comment: pr.comment,
      }));
    }

    // Create review
    const review = await this.reviewsRepository.create({
      orderId,
      customerId,
      customerName,
      shopId: order.shopId,
      rating,
      comment,
      productReviews: enrichedProductReviews,
    });

    // Update shop stats
    await this.updateShopStats(order.shopId);

    // Update product ratings and collect results
    let productRatingsUpdated: ProductRatingUpdate[] | undefined;
    if (enrichedProductReviews && enrichedProductReviews.length > 0) {
      productRatingsUpdated = [];
      
      for (const productReview of enrichedProductReviews) {
        try {
          const updatedStats = await this.updateProductRating(
            productReview.productId,
            productReview.rating,
          );
          
          if (updatedStats) {
            productRatingsUpdated.push({
              productId: productReview.productId,
              productName: productReview.productName,
              newRating: updatedStats.rating,
              totalRatings: updatedStats.totalRatings,
            });
          }
        } catch (error) {
          // Log error but don't fail the whole review
          this.logger.warn(
            `Failed to update rating for product ${productReview.productId}: ${error}`,
          );
        }
      }
    }

    return {
      review,
      productRatingsUpdated:
        productRatingsUpdated && productRatingsUpdated.length > 0
          ? productRatingsUpdated
          : undefined,
    };
  }

  /**
   * Update product rating with incremental calculation
   * Formula: newAvg = (oldAvg * totalRatings + newRating) / (totalRatings + 1)
   * 
   * @returns Updated rating stats or null if product not found
   */
  private async updateProductRating(
    productId: string,
    newRating: number,
  ): Promise<{ rating: number; totalRatings: number } | null> {
    try {
      // Get current product stats
      const product = await this.productsService.findOne(productId);
      if (!product) {
        this.logger.warn(`Product ${productId} not found when updating rating`);
        return null;
      }

      // Calculate new average
      const currentRating = product.rating || 0;
      const currentCount = product.totalRatings || 0;

      let newAvgRating: number;
      let newTotalRatings: number;

      if (currentCount === 0) {
        newAvgRating = newRating;
        newTotalRatings = 1;
      } else {
        newAvgRating =
          (currentRating * currentCount + newRating) / (currentCount + 1);
        newTotalRatings = currentCount + 1;
      }

      // Round to 1 decimal place
      newAvgRating = Math.round(newAvgRating * 10) / 10;

      // Update product stats
      await this.productsService.updateProductStats(productId, {
        rating: newAvgRating,
        totalRatings: newTotalRatings,
      });

      return {
        rating: newAvgRating,
        totalRatings: newTotalRatings,
      };
    } catch (error) {
      this.logger.error(`Error updating product rating for ${productId}:`, error);
      throw error;
    }
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
   * Get reviews for a specific product (public)
   * Returns reviews that contain ratings for this product
   */
  async getProductReviews(
    productId: string,
    options: { page?: number; limit?: number } = {},
  ): Promise<{
    reviews: Array<{
      reviewId: string;
      customerName: string;
      rating: number;
      comment?: string;
      createdAt: any;
    }>;
    total: number;
    avgRating: number;
    totalRatings: number;
  }> {
    const { reviews, total } = await this.reviewsRepository.findByProductId(
      productId,
      options,
    );

    // Map to simplified response for product reviews
    const productReviews = reviews.map((r) => ({
      reviewId: r.review.id || '',
      customerName: r.review.customerName || 'Khách hàng',
      rating: r.productReview.rating as number,
      comment: r.productReview.comment as string | undefined,
      createdAt: r.review.createdAt?.toDate?.()?.toISOString?.() ?? r.review.createdAt,
    }));

    // Calculate average rating
    let avgRating = 0;
    if (productReviews.length > 0) {
      const sum = productReviews.reduce((acc, r) => acc + r.rating, 0);
      avgRating = Math.round((sum / productReviews.length) * 10) / 10;
    }

    return {
      reviews: productReviews,
      total,
      avgRating,
      totalRatings: total,
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
