import {
  Injectable,
  CanActivate,
  ExecutionContext,
  UnauthorizedException,
  Logger,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { FirebaseService } from '../firebase/firebase.service';
import { IS_PUBLIC_KEY } from '../decorators';
import { IUser, UserRole } from '../interfaces/user.interface';

/**
 * Auth Guard
 *
 * Validates Firebase ID token from Authorization header.
 * Attaches decoded user to request.user.
 *
 * Usage:
 *   @UseGuards(AuthGuard)
 *   @Controller('protected')
 */
@Injectable()
export class AuthGuard implements CanActivate {
  private readonly logger = new Logger(AuthGuard.name);

  constructor(
    private readonly firebaseService: FirebaseService,
    private readonly reflector: Reflector,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    // Check if route is marked as public
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);

    if (isPublic) {
      return true;
    }

    const request = context.switchToHttp().getRequest();
    const token = this.extractTokenFromHeader(request);

    if (!token) {
      throw new UnauthorizedException('Missing authorization token');
    }

    try {
      const decodedToken = await this.firebaseService.auth.verifyIdToken(token);

      // Get user custom claims for role
      const userRecord = await this.firebaseService.auth.getUser(decodedToken.uid);
      const customClaims = userRecord.customClaims || {};

      // Attach user to request
      const user: IUser = {
        uid: decodedToken.uid,
        email: decodedToken.email,
        displayName: decodedToken.name,
        phone: decodedToken.phone_number,
        role: (customClaims.role as UserRole) || UserRole.CUSTOMER,
        emailVerified: decodedToken.email_verified,
        photoUrl: decodedToken.picture,
      };

      request.user = user;
      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      this.logger.error(`Token verification failed: ${message}`);
      throw new UnauthorizedException('Invalid or expired token');
    }
  }

  private extractTokenFromHeader(request: Request & { headers: Record<string, string> }): string | undefined {
    const authHeader = request.headers['authorization'];
    if (!authHeader) return undefined;

    const [type, token] = authHeader.split(' ');
    return type === 'Bearer' ? token : undefined;
  }
}
