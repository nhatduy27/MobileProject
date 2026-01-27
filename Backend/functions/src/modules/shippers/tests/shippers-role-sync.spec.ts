import { Test, TestingModule } from '@nestjs/testing';
import { ShippersService } from '../shippers.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { IShippersRepository } from '../repositories/shippers-repository.interface';
import { UsersService } from '../../users/users.service';
import { ShopsService } from '../../shops/services/shops.service';
import { NotificationsService } from '../../notifications/services/notifications.service';
import { StorageService } from '../../../shared/services/storage.service';
import { NotFoundException, ForbiddenException, ConflictException } from '@nestjs/common';
import { ShipperApplicationEntity, ApplicationStatus } from '../entities/shipper-application.entity';
import { Firestore } from '@google-cloud/firestore';
import { WalletsService } from '../../wallets/wallets.service';

/**
 * Shippers Service - Role Synchronization Tests
 *
 * Verifies that:
 * 1. approveApplication() updates both Firestore and Firebase Custom Claims
 * 2. removeShipper() reverts role in both sources
 * 3. Claims update failures are handled properly
 */
describe('ShippersService - Role Synchronization', () => {
  let service: ShippersService;
  let firebaseService: jest.Mocked<FirebaseService>;
  let shippersRepository: jest.Mocked<IShippersRepository>;
  let usersService: jest.Mocked<UsersService>;
  let shopsService: jest.Mocked<ShopsService>;
  let storageService: jest.Mocked<StorageService>;
  let firestore: jest.Mocked<Firestore>;

  const mockShipperApp: ShipperApplicationEntity = {
    id: 'app_123',
    userId: 'shipper_user_123',
    userName: 'Test Shipper',
    userPhone: '0901234567',
    userAvatar: 'https://...',
    shopId: 'shop_456',
    shopName: 'Test Shop',
    vehicleType: 'BIKE' as any,
    vehicleNumber: 'ABC123',
    idCardNumber: '123456789',
    idCardFrontUrl: 'https://...',
    idCardBackUrl: 'https://...',
    driverLicenseUrl: 'https://...',
    message: 'I want to be a shipper',
    status: ApplicationStatus.PENDING,
    createdAt: new Date(),
  };

  const mockShop = {
    id: 'shop_456',
    name: 'Test Shop',
    ownerId: 'owner_123',
  };

  beforeEach(async () => {
    // Mock Firestore collection and transaction
    const mockDocRef = {
      update: jest.fn().mockResolvedValue(undefined),
    };
    const mockCollectionRef = {
      doc: jest.fn().mockReturnValue(mockDocRef),
    };

    const mockTransaction = {
      update: jest.fn(),
      get: jest.fn().mockResolvedValue({
        data: () => ({
          status: 'PENDING',
        }),
      }),
    };

    firebaseService = {
      auth: {
        setCustomUserClaims: jest.fn().mockResolvedValue(undefined),
      },
    } as any;

    firestore = {
      runTransaction: jest.fn(async (callback) => {
        await callback(mockTransaction);
      }),
      collection: jest.fn().mockReturnValue(mockCollectionRef),
    } as any;

    shippersRepository = {
      findApplicationById: jest.fn(),
      findShopApplications: jest.fn(),
      updateApplicationStatus: jest.fn(),
      findUserApplications: jest.fn(),
      deleteApplication: jest.fn(),
      createApplication: jest.fn(),
      findShippersByShop: jest.fn(),
      getApplicationDocRef: jest.fn(() => ({})),
      getUserDocRef: jest.fn(() => ({})),
    } as any;

    usersService = {
      getProfile: jest.fn(),
    } as any;

    shopsService = {
      getMyShop: jest.fn(),
    } as any;

    storageService = {} as any;

    const mockNotificationsService = {
      send: jest.fn().mockResolvedValue(undefined),
      sendToTopic: jest.fn().mockResolvedValue(undefined),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ShippersService,
        {
          provide: FirebaseService,
          useValue: firebaseService,
        },
        {
          provide: 'IShippersRepository',
          useValue: shippersRepository,
        },
        {
          provide: UsersService,
          useValue: usersService,
        },
        {
          provide: ShopsService,
          useValue: shopsService,
        },
        {
          provide: NotificationsService,
          useValue: mockNotificationsService,
        },
        {
          provide: StorageService,
          useValue: storageService,
        },
        {
          provide: WalletsService,
          useValue: {
            processOrderPayout: jest.fn().mockResolvedValue(undefined),
            updateBalance: jest.fn().mockResolvedValue(undefined),
            initializeWallet: jest.fn().mockResolvedValue(undefined),
          },
        },
        {
          provide: 'FIRESTORE',
          useValue: firestore,
        },
      ],
    }).compile();

    service = module.get<ShippersService>(ShippersService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('approveApplication() - Shipper Approval with Claims Update', () => {
    it('should approve application AND update Firebase Custom Claims', async () => {
      const ownerId = 'owner_123';

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      shippersRepository.findApplicationById.mockResolvedValueOnce(mockShipperApp);
      (firebaseService.auth.setCustomUserClaims as jest.Mock).mockResolvedValueOnce(undefined);

      await service.approveApplication(ownerId, mockShipperApp.id);

      // Verify custom claims was updated to SHIPPER
      expect(firebaseService.auth.setCustomUserClaims).toHaveBeenCalledWith(
        mockShipperApp.userId,
        {
          role: 'SHIPPER',
        }
      );
    });

    it('should log error if custom claims update fails but NOT throw', async () => {
      const ownerId = 'owner_123';

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      shippersRepository.findApplicationById.mockResolvedValueOnce(mockShipperApp);
      (firebaseService.auth.setCustomUserClaims as jest.Mock).mockRejectedValueOnce(
        new Error('Firebase error')
      );

      // P0-FIX: approveApplication should NOT throw when claims sync fails
      // It updates Firestore successfully, only claims sync fails
      // User can re-login to get fresh claims
      await expect(service.approveApplication(ownerId, mockShipperApp.id)).resolves.not.toThrow();
    });

    it('should throw if application not found', async () => {
      const ownerId = 'owner_123';

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      shippersRepository.findApplicationById.mockResolvedValueOnce(null);

      await expect(
        service.approveApplication(ownerId, 'nonexistent_app')
      ).rejects.toThrow(NotFoundException);
    });

    it('should throw if owner does not own shop', async () => {
      const ownerId = 'wrong_owner';
      const appWithDifferentShop = { ...mockShipperApp, shopId: 'shop_999' };

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      shippersRepository.findApplicationById.mockResolvedValueOnce(appWithDifferentShop);

      await expect(service.approveApplication(ownerId, appWithDifferentShop.id)).rejects.toThrow(
        ForbiddenException
      );
    });

    it('should throw if application already processed', async () => {
      const ownerId = 'owner_123';
      const approvedApp = { ...mockShipperApp, status: ApplicationStatus.APPROVED };

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      shippersRepository.findApplicationById.mockResolvedValueOnce(approvedApp);

      await expect(service.approveApplication(ownerId, approvedApp.id)).rejects.toThrow(
        ConflictException
      );
    });
  });

  describe('removeShipper() - Shipper Removal with Claims Revert', () => {
    it('should remove shipper AND revert Firebase Custom Claims to CUSTOMER', async () => {
      const ownerId = 'owner_123';
      const shipperId = 'shipper_user_123';

      const shipperUser = {
        id: shipperId,
        displayName: 'Test Shipper',
        phone: '0901234567',
        role: 'SHIPPER',
        shipperInfo: {
          shopId: mockShop.id,
          shopName: mockShop.name,
          status: 'ACTIVE',
        },
      };

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      usersService.getProfile.mockResolvedValueOnce(shipperUser as any);
      (firebaseService.auth.setCustomUserClaims as jest.Mock).mockResolvedValueOnce(undefined);

      await service.removeShipper(ownerId, shipperId);

      // Verify custom claims was reverted to CUSTOMER
      expect(firebaseService.auth.setCustomUserClaims).toHaveBeenCalledWith(shipperId, {
        role: 'CUSTOMER',
      });
    });

    it('should throw if custom claims revert fails', async () => {
      const ownerId = 'owner_123';
      const shipperId = 'shipper_user_123';

      const shipperUser = {
        id: shipperId,
        displayName: 'Test Shipper',
        role: 'SHIPPER',
        shipperInfo: {
          shopId: mockShop.id,
          shopName: mockShop.name,
        },
      };

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      usersService.getProfile.mockResolvedValueOnce(shipperUser as any);
      (firebaseService.auth.setCustomUserClaims as jest.Mock).mockRejectedValueOnce(
        new Error('Firebase error')
      );

      await expect(service.removeShipper(ownerId, shipperId)).rejects.toThrow(
        /Failed to sync Firebase custom claims/
      );
    });

    it('should throw if shipper not found', async () => {
      const ownerId = 'owner_123';
      const shipperId = 'nonexistent_shipper';

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      usersService.getProfile.mockRejectedValueOnce(new Error('User not found'));

      await expect(service.removeShipper(ownerId, shipperId)).rejects.toThrow();
    });

    it('should throw if shipper not in owners shop', async () => {
      const ownerId = 'owner_123';
      const shipperId = 'shipper_user_123';

      const shipperUser = {
        id: shipperId,
        displayName: 'Test Shipper',
        role: 'SHIPPER',
        shipperInfo: {
          shopId: 'shop_999', // Different shop
          shopName: 'Other Shop',
        },
      };

      shopsService.getMyShop.mockResolvedValueOnce(mockShop as any);
      usersService.getProfile.mockResolvedValueOnce(shipperUser as any);

      await expect(service.removeShipper(ownerId, shipperId)).rejects.toThrow(
        ForbiddenException
      );
    });
  });
});
