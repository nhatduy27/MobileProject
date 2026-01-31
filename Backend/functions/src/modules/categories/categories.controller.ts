import { Controller, Get, Param } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { CategoriesService } from './categories.service';
import { CategoryEntity } from './entities/category.entity';

// Helper để serialize Timestamp → ISO string
const serializeTimestamp = (ts: any): string | null => {
  if (!ts) return null;
  if (typeof ts === 'string') return ts;
  if (ts.toDate) return ts.toDate().toISOString();
  if (ts._seconds) return new Date(ts._seconds * 1000).toISOString();
  return null;
};

// Helper để serialize category
const serializeCategory = (category: CategoryEntity) => ({
  ...category,
  createdAt: serializeTimestamp(category.createdAt),
  updatedAt: serializeTimestamp(category.updatedAt),
});

/**
 * Categories Controller - Public API
 *
 * Lấy danh sách categories cho khách hàng/users
 * Không cần xác thực
 *
 * Base URL: /categories
 *
 * Theo docs: docs-god/api/05_CATEGORIES_API.md
 */
@ApiTags('Categories')
@Controller('categories')
export class CategoriesController {
  constructor(private readonly categoriesService: CategoriesService) {}

  /**
   * GET /categories
   * Lấy danh sách categories active
   */
  @Get()
  @ApiOperation({ summary: 'Lấy danh sách categories (chỉ active)' })
  @ApiResponse({
    status: 200,
    description: 'Danh sách categories',
  })
  async findActive() {
    const categories = await this.categoriesService.findActive();
    return categories.map(serializeCategory);
  }

  /**
   * GET /categories/:idOrSlug
   * Lấy chi tiết category theo ID hoặc slug
   */
  @Get(':idOrSlug')
  @ApiOperation({ summary: 'Lấy chi tiết category theo ID hoặc slug' })
  @ApiResponse({
    status: 200,
    description: 'Chi tiết category',
  })
  @ApiResponse({ status: 404, description: 'Category không tồn tại' })
  async findOne(@Param('idOrSlug') idOrSlug: string) {
    // Thử tìm theo slug trước
    let category = await this.categoriesService.findBySlug(idOrSlug);

    // Nếu không tìm thấy, thử tìm theo ID
    if (!category) {
      category = await this.categoriesService.findById(idOrSlug);
    }

    return serializeCategory(category);
  }
}
