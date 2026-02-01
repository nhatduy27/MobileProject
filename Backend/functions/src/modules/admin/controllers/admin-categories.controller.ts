import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { CategoriesService } from '../../categories/categories.service';
import { CreateCategoryDto, UpdateCategoryDto } from '../../categories/dto';
import { AuthGuard, AdminGuard } from '../../../core/guards';
import { CategoryEntity } from '../../categories/entities/category.entity';

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
 * Admin Categories Controller
 *
 * CRUD operations cho categories - chỉ dành cho Admin
 *
 * Base URL: /admin/categories
 *
 * Theo docs: docs-god/api/22_ADMIN_API.md
 */
@ApiTags('Admin - Categories')
@ApiBearerAuth()
@UseGuards(AuthGuard, AdminGuard)
@Controller('admin/categories')
export class AdminCategoriesController {
  constructor(private readonly categoriesService: CategoriesService) {}

  /**
   * POST /admin/categories
   * Tạo category mới
   */
  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Tạo category mới' })
  @ApiResponse({
    status: 201,
    description: 'Category đã được tạo thành công',
  })
  @ApiResponse({ status: 400, description: 'Dữ liệu không hợp lệ' })
  @ApiResponse({ status: 401, description: 'Chưa xác thực' })
  @ApiResponse({ status: 403, description: 'Không có quyền Admin' })
  async create(@Body() dto: CreateCategoryDto) {
    const category = await this.categoriesService.create(dto);
    return serializeCategory(category);
  }

  /**
   * GET /admin/categories
   * Lấy tất cả categories (bao gồm inactive)
   */
  @Get()
  @ApiOperation({ summary: 'Lấy tất cả categories (bao gồm inactive)' })
  @ApiResponse({
    status: 200,
    description: 'Danh sách categories',
  })
  @ApiResponse({ status: 401, description: 'Chưa xác thực' })
  @ApiResponse({ status: 403, description: 'Không có quyền Admin' })
  async findAll() {
    const categories = await this.categoriesService.findAll();
    return categories.map(serializeCategory);
  }

  /**
   * GET /admin/categories/:id
   * Lấy chi tiết category theo ID
   */
  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết category' })
  @ApiResponse({
    status: 200,
    description: 'Chi tiết category',
  })
  @ApiResponse({ status: 401, description: 'Chưa xác thực' })
  @ApiResponse({ status: 403, description: 'Không có quyền Admin' })
  @ApiResponse({ status: 404, description: 'Category không tồn tại' })
  async findOne(@Param('id') id: string) {
    const category = await this.categoriesService.findById(id);
    return serializeCategory(category);
  }

  /**
   * PUT /admin/categories/:id
   * Cập nhật category
   */
  @Put(':id')
  @ApiOperation({ summary: 'Cập nhật category' })
  @ApiResponse({
    status: 200,
    description: 'Category đã được cập nhật',
  })
  @ApiResponse({ status: 400, description: 'Dữ liệu không hợp lệ' })
  @ApiResponse({ status: 401, description: 'Chưa xác thực' })
  @ApiResponse({ status: 403, description: 'Không có quyền Admin' })
  @ApiResponse({ status: 404, description: 'Category không tồn tại' })
  @ApiResponse({ status: 409, description: 'Slug đã tồn tại' })
  async update(@Param('id') id: string, @Body() dto: UpdateCategoryDto) {
    const category = await this.categoriesService.update(id, dto);
    return serializeCategory(category);
  }

  /**
   * DELETE /admin/categories/:id
   * Xóa category
   */
  @Delete(':id')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Xóa category' })
  @ApiResponse({
    status: 200,
    description: 'Category đã được xóa',
  })
  @ApiResponse({ status: 401, description: 'Chưa xác thực' })
  @ApiResponse({ status: 403, description: 'Không có quyền Admin' })
  @ApiResponse({ status: 404, description: 'Category không tồn tại' })
  @ApiResponse({ status: 409, description: 'Không thể xóa category có products' })
  async remove(@Param('id') id: string) {
    await this.categoriesService.delete(id);
    return { message: 'Category deleted successfully' };
  }
}
