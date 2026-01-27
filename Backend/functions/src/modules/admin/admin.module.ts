import { Module } from '@nestjs/common';
import {
  AdminCategoriesController,
  AdminUsersController,
  AdminPayoutsController,
  AdminShopsController,
  AdminDashboardController,
} from './controllers';
import { AdminService } from './admin.service';
import { CategoriesModule } from '../categories';
import { WalletsModule } from '../wallets';
import {
  ADMIN_USERS_REPOSITORY_TOKEN,
  ADMIN_SHOPS_REPOSITORY_TOKEN,
  ADMIN_PAYOUTS_REPOSITORY_TOKEN,
} from './interfaces';
import {
  FirestoreAdminUsersRepository,
  FirestoreAdminShopsRepository,
  FirestoreAdminPayoutsRepository,
} from './repositories';

/**
 * Admin Module - Module quản trị hệ thống
 *
 * Module này chứa tất cả các controller và service dành cho Admin.
 * Tất cả route trong module này được bảo vệ bởi AdminGuard.
 *
 * Dependency Injection:
 * - ADMIN_USERS_REPOSITORY_TOKEN -> FirestoreAdminUsersRepository
 * - ADMIN_SHOPS_REPOSITORY_TOKEN -> FirestoreAdminShopsRepository
 * - ADMIN_PAYOUTS_REPOSITORY_TOKEN -> FirestoreAdminPayoutsRepository
 *
 * Controllers:
 * - AdminCategoriesController: CRUD categories (ADMIN-002)
 * - AdminUsersController: List/Ban users (ADMIN-006, 007)
 * - AdminPayoutsController: Manage payouts (ADMIN-008-011)
 * - AdminShopsController: Manage shops (ADMIN-012)
 * - AdminDashboardController: Dashboard stats (ADMIN-013)
 */
@Module({
  imports: [
    // Import CategoriesModule để sử dụng CategoriesService
    CategoriesModule,
    
    // Import WalletsModule để process payout transfers
    WalletsModule,

    // TODO: Import các modules khác khi implement các EPIC tương ứng
    // UsersModule (EPIC 03),
    // ShopsModule (EPIC 04),
    // OrdersModule (EPIC 07),
    // NotificationsModule (EPIC 11),
  ],
  controllers: [
    AdminCategoriesController,
    AdminUsersController,
    AdminPayoutsController,
    AdminShopsController,
    AdminDashboardController,
  ],
  providers: [
    // Register repositories với interface tokens
    {
      provide: ADMIN_USERS_REPOSITORY_TOKEN,
      useClass: FirestoreAdminUsersRepository,
    },
    {
      provide: ADMIN_SHOPS_REPOSITORY_TOKEN,
      useClass: FirestoreAdminShopsRepository,
    },
    {
      provide: ADMIN_PAYOUTS_REPOSITORY_TOKEN,
      useClass: FirestoreAdminPayoutsRepository,
    },
    AdminService,
  ],
  exports: [AdminService],
})
export class AdminModule {}
