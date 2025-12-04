import {
  Injectable,
  CanActivate,
  ExecutionContext,
  UnauthorizedException,
} from '@nestjs/common';
import { Observable } from 'rxjs';

/**
 * Auth Guard (Placeholder)
 * 
 * Validates JWT tokens for protected routes.
 * TODO: Implement actual JWT validation using @nestjs/jwt
 */
@Injectable()
export class AuthGuard implements CanActivate {
  canActivate(
    context: ExecutionContext,
  ): boolean | Promise<boolean> | Observable<boolean> {
    const request = context.switchToHttp().getRequest();
    const token = this.extractTokenFromHeader(request);

    if (!token) {
      throw new UnauthorizedException('No token provided');
    }

    // TODO: Validate JWT token
    // try {
    //   const payload = await this.jwtService.verifyAsync(token);
    //   request.user = payload;
    // } catch {
    //   throw new UnauthorizedException('Invalid token');
    // }

    // For now, just pass through (stub implementation)
    return true;
  }

  private extractTokenFromHeader(request: any): string | undefined {
    const [type, token] = request.headers.authorization?.split(' ') ?? [];
    return type === 'Bearer' ? token : undefined;
  }
}
