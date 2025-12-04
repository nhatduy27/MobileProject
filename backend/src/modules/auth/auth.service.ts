import { Injectable, Logger, UnauthorizedException, ConflictException } from '@nestjs/common';
import { AuthRepository } from './domain/auth.repository';
import { RegisterDto, LoginDto, AuthResponseDto } from './dto/auth.dto';

/**
 * Auth Service (Application Layer)
 * 
 * Contains the business logic for authentication operations.
 * Depends on AuthRepository abstraction (Dependency Inversion).
 */
@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(private readonly authRepository: AuthRepository) {}

  /**
   * Register a new user
   */
  async register(dto: RegisterDto): Promise<AuthResponseDto> {
    this.logger.log(`Registering new user: ${dto.email}`);

    // Check if user already exists
    const existingUser = await this.authRepository.findByEmail(dto.email);
    if (existingUser) {
      throw new ConflictException('User with this email already exists');
    }

    // TODO: Hash password properly using bcrypt or similar
    const passwordHash = `hashed_${dto.password}`;

    // Create user
    const user = await this.authRepository.create({
      email: dto.email,
      passwordHash,
      roles: ['user'], // Default role
    });

    // TODO: Generate JWT token
    const accessToken = `jwt_token_for_${user.id}`;

    return {
      user: {
        id: user.id,
        email: user.email,
        roles: user.roles,
      },
      accessToken,
    };
  }

  /**
   * Authenticate a user
   */
  async login(dto: LoginDto): Promise<AuthResponseDto> {
    this.logger.log(`User login attempt: ${dto.email}`);

    // Find user by email
    const user = await this.authRepository.findByEmail(dto.email);
    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    // TODO: Verify password using bcrypt.compare()
    const isPasswordValid = user.passwordHash === `hashed_${dto.password}`;
    if (!isPasswordValid) {
      throw new UnauthorizedException('Invalid credentials');
    }

    // TODO: Generate JWT token
    const accessToken = `jwt_token_for_${user.id}`;

    return {
      user: {
        id: user.id,
        email: user.email,
        roles: user.roles,
      },
      accessToken,
    };
  }

  /**
   * Get user by ID
   */
  async getUserById(userId: string) {
    const user = await this.authRepository.findById(userId);
    if (!user) {
      throw new UnauthorizedException('User not found');
    }

    return {
      id: user.id,
      email: user.email,
      roles: user.roles,
    };
  }
}
