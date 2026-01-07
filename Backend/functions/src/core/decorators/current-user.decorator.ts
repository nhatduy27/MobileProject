import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { IUser } from '../interfaces/user.interface';

/**
 * Current User Decorator
 *
 * Extracts the authenticated user from request.
 * Can optionally extract a specific property.
 *
 * Usage:
 *   @Get('profile')
 *   getProfile(@CurrentUser() user: IUser) { ... }
 *
 *   @Get('my-orders')
 *   getMyOrders(@CurrentUser('uid') userId: string) { ... }
 */
export const CurrentUser = createParamDecorator(
  (data: keyof IUser | undefined, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest();
    const user: IUser = request.user;

    if (!user) {
      return null;
    }

    return data ? user[data] : user;
  },
);
