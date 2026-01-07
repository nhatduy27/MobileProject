import { Injectable, NotFoundException, Logger } from '@nestjs/common';
import { CategoriesRepository } from './categories.repository';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { Category } from './entities/category.entity';

@Injectable()
export class CategoriesService {
  private readonly logger = new Logger(CategoriesService.name);

  constructor(private readonly categoriesRepository: CategoriesRepository) {}

  async findAll(includeInactive = false): Promise<Category[]> {
    this.logger.log(`Finding all categories, includeInactive: ${includeInactive}`);

    if (includeInactive) {
      return this.categoriesRepository.findAll();
    }
    return this.categoriesRepository.findActive();
  }

  async findById(id: string): Promise<Category> {
    this.logger.log(`Finding category by id: ${id}`);

    const category = await this.categoriesRepository.findById(id);
    if (!category) {
      throw new NotFoundException(`Category with ID ${id} not found`);
    }
    return category;
  }

  async create(dto: CreateCategoryDto): Promise<Category> {
    this.logger.log(`Creating category: ${dto.name}`);

    const category = await this.categoriesRepository.create({
      name: dto.name,
      description: dto.description || '',
      iconUrl: dto.iconUrl || '',
      sortOrder: dto.sortOrder || 0,
      isActive: true,
    });

    this.logger.log(`Category created with id: ${category.id}`);
    return category;
  }

  async update(id: string, dto: UpdateCategoryDto): Promise<Category> {
    this.logger.log(`Updating category: ${id}`);

    // Check if exists
    await this.findById(id);

    const updated = await this.categoriesRepository.update(id, dto);
    if (!updated) {
      throw new NotFoundException(`Category with ID ${id} not found`);
    }

    this.logger.log(`Category updated: ${id}`);
    return updated;
  }

  async delete(id: string): Promise<void> {
    this.logger.log(`Deleting category: ${id}`);

    // Check if exists
    await this.findById(id);

    // Soft delete - just set isActive to false
    await this.categoriesRepository.update(id, { isActive: false });

    this.logger.log(`Category soft deleted: ${id}`);
  }
}
