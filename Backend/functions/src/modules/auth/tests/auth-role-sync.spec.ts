import { Test, TestingModule } from '@nestjs/testing';
import { AuthService } from '../auth.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { IUsersRepository, USERS_REPOSITORY_TOKEN } from '../interfaces';
import { IOTPRepository, OTP_REPOSITORY_TOKEN } from '../interfaces';
import { EmailService } from '../../email/email.service';
import { UserRole } from '../entities';
import { ConflictException } from '@nestjs/common';

/**
 * Auth Service - Role Synchronization Tests
 *
 * Verifies that:
 * 1. Role is synced between Firestore and Firebase Custom Claims on registration
 * 2. AuthService.setRole() updates both sources
 * 3. Registration stores dto.role (not hardcoded CUSTOMER) in Firestore
 */
describe('AuthService - Role Synchronization', () => {
  let service: AuthService;
  let firebaseService: jest.Mocked<FirebaseService>;
  let usersRepository: jest.Mocked<IUsersRepository>;
  let otpRepository: jest.Mocked<IOTPRepository>;
  let emailService: jest.Mocked<EmailService>;

  beforeEach(async () => {
    // Mock Firebase service
    firebaseService = {
      auth: {
        createUser: jest.fn(),
        setCustomUserClaims: jest.fn().mockResolvedValue(undefined),
        createCustomToken: jest.fn().mockResolvedValue('custom_token'),
        verifyIdToken: jest.fn(),
        getUser: jest.fn(),
      },
    } as any;

    // Mock repositories
    usersRepository = {
      findByEmail: jest.fn(),
      findByPhone: jest.fn(),
      createWithId: jest.fn(),
      findById: jest.fn(),
      update: jest.fn(),
    } as any;

    otpRepository = {} as any;
    emailService = {
      sendWelcomeEmail: jest.fn().mockReturnValue(Promise.resolve()),
    } as any;

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        {
          provide: FirebaseService,
          useValue: firebaseService,
        },
        {
          provide: USERS_REPOSITORY_TOKEN,
          useValue: usersRepository,
        },
        {
          provide: OTP_REPOSITORY_TOKEN,
          useValue: otpRepository,
        },
        {
          provide: EmailService,
          useValue: emailService,
        },
      ],
    }).compile();

    service = module.get<AuthService>(AuthService);
  });

  describe('register() - Role Synchronization', () => {
    it('should store dto.role in Firestore (not hardcoded CUSTOMER)', async () => {
      const registerDto = {
        email: 'owner@test.com',
        password: 'password123',
        displayName: 'Test Owner',
        phone: '0901234567',
        role: UserRole.OWNER,
      };

      const mockUser = {
        uid: 'user_123',
        email: registerDto.email,
        displayName: registerDto.displayName,
      };

      usersRepository.findByEmail.mockResolvedValueOnce(null);
      usersRepository.findByPhone.mockResolvedValueOnce(null);
      (firebaseService.auth.createUser as jest.Mock).mockResolvedValueOnce(mockUser);
      usersRepository.createWithId.mockResolvedValueOnce({} as any);

      await service.register(registerDto);

      // Verify setCustomUserClaims was called with OWNER role
      expect(firebaseService.auth.setCustomUserClaims).toHaveBeenCalledWith(mockUser.uid, {
        role: UserRole.OWNER,
      });

      // Verify createWithId was called with OWNER role in Firestore
      const createCall = usersRepository.createWithId.mock.calls[0];
      const firebaseEntity = createCall[1];
      expect(firebaseEntity.role).toBe(UserRole.OWNER); // ← CRITICAL: NOT CUSTOMER
    });

    it('should sync custom claims and Firestore role for CUSTOMER', async () => {
      const registerDto = {
        email: 'customer@test.com',
        password: 'password123',
        displayName: 'Test Customer',
        phone: '0901234567',
        role: UserRole.CUSTOMER,
      };

      const mockUser = {
        uid: 'user_456',
        email: registerDto.email,
      };

      usersRepository.findByEmail.mockResolvedValueOnce(null);
      usersRepository.findByPhone.mockResolvedValueOnce(null);
      (firebaseService.auth.createUser as jest.Mock).mockResolvedValueOnce(mockUser);
      usersRepository.createWithId.mockResolvedValueOnce({} as any);

      await service.register(registerDto);

      // Both sources should have CUSTOMER role
      expect(firebaseService.auth.setCustomUserClaims).toHaveBeenCalledWith(mockUser.uid, {
        role: UserRole.CUSTOMER,
      });

      const createCall = usersRepository.createWithId.mock.calls[0];
      const firebaseEntity = createCall[1];
      expect(firebaseEntity.role).toBe(UserRole.CUSTOMER);
    });

    it('should throw if email already exists', async () => {
      const registerDto = {
        email: 'existing@test.com',
        password: 'password123',
        displayName: 'Test User',
        role: UserRole.CUSTOMER,
      };

      usersRepository.findByEmail.mockResolvedValueOnce({ id: 'user_existing' } as any);

      await expect(service.register(registerDto)).rejects.toThrow(ConflictException);
      expect(firebaseService.auth.createUser).not.toHaveBeenCalled();
    });
  });

  describe('setRole() - Updates Both Sources', () => {
    it('should update both Firestore and Firebase Custom Claims', async () => {
      const userId = 'user_123';
      const newRole = UserRole.SHIPPER;

      (usersRepository.update as jest.Mock).mockResolvedValueOnce(undefined);
      (firebaseService.auth.setCustomUserClaims as jest.Mock).mockResolvedValueOnce(undefined);

      const result = await service.setRole(userId, newRole);

      // Verify both updates were called
      expect(usersRepository.update).toHaveBeenCalledWith(userId, { role: newRole });
      expect(firebaseService.auth.setCustomUserClaims).toHaveBeenCalledWith(userId, {
        role: newRole,
      });

      // Verify response
      expect(result.role).toBe(UserRole.SHIPPER);
      expect(result.message).toBe('Role updated successfully');
    });

    it('should throw if Firestore update fails', async () => {
      const userId = 'user_123';
      const newRole = UserRole.OWNER;

      (usersRepository.update as jest.Mock).mockRejectedValueOnce(new Error('DB error'));

      await expect(service.setRole(userId, newRole)).rejects.toThrow('DB error');
      expect(firebaseService.auth.setCustomUserClaims).not.toHaveBeenCalled();
    });

    it('should throw if custom claims update fails', async () => {
      const userId = 'user_123';
      const newRole = UserRole.ADMIN;

      (usersRepository.update as jest.Mock).mockResolvedValueOnce(undefined);
      (firebaseService.auth.setCustomUserClaims as jest.Mock).mockRejectedValueOnce(
        new Error('Firebase error')
      );

      await expect(service.setRole(userId, newRole)).rejects.toThrow('Firebase error');
    });
  });
});
