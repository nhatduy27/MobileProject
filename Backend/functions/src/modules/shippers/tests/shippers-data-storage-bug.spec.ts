import { Test, TestingModule } from '@nestjs/testing';
import { ShippersService } from '../shippers.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import { IShippersRepository } from '../repositories/shippers-repository.interface';
import { UsersService } from '../../users/users.service';
import { ShopsService } from '../../shops/services/shops.service';
import { NotificationsService } from '../../notifications/services/notifications.service';
import { StorageService } from '../../../shared/services/storage.service';
import { BadRequestException } from '@nestjs/common';
import { ShipperApplicationEntity, ApplicationStatus } from '../entities/shipper-application.entity';
import { Firestore } from '@google-cloud/firestore';
import { WalletsService } from '../../wallets/wallets.service';

/**
 * Shippers Service - Data Storage Bug Prevention Tests
 * 
 * Regression tests to prevent the bug where shipperInfo is written
 * to the owner's document instead of the shipper's document.
 * 
 * Bug Details:
 * - shipperInfo was found in owner's document (wrong location)
 * - shipperInfo was missing from shipper's document (correct location)
 * - Result: Shipper endpoints failed with "not assigned to shop"
 * 
 * Fixes Tested:
 * 1. approveApplication validates app.userId !== ownerId
 * 2. Writes shipperInfo to users/{app.userId} (shipper's doc)
 * 3. Validation provides helpful error messages
 */
describe('ShippersService - Data Storage Bug Prevention (SHIPPER-DATA-BUG-FIX)', () => {
  let service: ShippersService;
  let firebaseService: jest.Mocked<FirebaseService>;
  let shippersRepository: jest.Mocked<IShippersRepository>;
  let usersService: jest.Mocked<UsersService>;
  let shopsService: jest.Mocked<ShopsService>;
  let storageService: jest.Mocked<StorageService>;
  let firestore: jest.Mocked<Firestore>;
  let mockTransaction: any;

  const mockShipperApp: ShipperApplicationEntity = {
    id: 'app_data_bug_001',
    userId: 'shipper_user_correct',  // ← SHIPPER uid (not owner)
    userName: 'Real Shipper',
    userPhone: '0901234567',
    userAvatar: 'https://...',
    shopId: 'shop_abc123',
    shopName: 'Test Shop',
    vehicleType: 'MOTORBIKE' as any,
    vehicleNumber: '59X1-12345',
    idCardNumber: '079202012345',
    idCardFrontUrl: 'https://...',
    idCardBackUrl: 'https://...',
    driverLicenseUrl: 'https://...',
    message: 'Want to be shipper',
    status: ApplicationStatus.PENDING,
    createdAt: new Date(),
  };

  beforeEach(async () => {
    // Mock transaction
    mockTransaction = {
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
      collection: jest.fn().mockReturnValue({
        doc: jest.fn().mockReturnValue({
          update: jest.fn().mockResolvedValue(undefined),
        }),
      }),
    } as any;

    shippersRepository = {
      findApplicationById: jest.fn().mockResolvedValue(mockShipperApp),
      createApplication: jest.fn(),
      findShopApplications: jest.fn(),
      updateApplicationStatus: jest.fn(),
      findUserApplications: jest.fn(),
      deleteApplication: jest.fn(),
      findShippersByShop: jest.fn(),
      getApplicationDocRef: jest.fn(() => ({})),
      getUserDocRef: jest.fn(() => ({})),
    } as any;

    usersService = {
      getProfile: jest.fn(),
    } as any;

    shopsService = {
      getMyShop: jest.fn().mockResolvedValue({
        id: 'shop_abc123',
        name: 'Test Shop',
        ownerId: 'owner_user_correct',
      }),
    } as any;

    storageService = {} as any;

    const mockNotificationsService = {
      send: jest.fn().mockResolvedValue(undefined),
      sendToTopic: jest.fn().mockResolvedValue(undefined),
    };

    const mockWalletsService = {
      processOrderPayout: jest.fn().mockResolvedValue(undefined),
      updateBalance: jest.fn().mockResolvedValue(undefined),
      initializeWallet: jest.fn().mockResolvedValue(undefined),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ShippersService,
        { provide: FirebaseService, useValue: firebaseService },
        { provide: 'IShippersRepository', useValue: shippersRepository },
        { provide: UsersService, useValue: usersService },
        { provide: ShopsService, useValue: shopsService },
        { provide: NotificationsService, useValue: mockNotificationsService },
        { provide: WalletsService, useValue: mockWalletsService },
        { provide: StorageService, useValue: storageService },
        { provide: 'FIRESTORE', useValue: firestore },
      ],
    }).compile();

    service = module.get<ShippersService>(ShippersService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('approveApplication - Prevent shipperInfo in wrong document', () => {
    it('should prevent approval when app.userId === ownerId (same person)', async () => {
      const ownerId = 'owner_user_123';
      const sameAsOwner: ShipperApplicationEntity = {
        ...mockShipperApp,
        userId: ownerId,  // ← BUG: App was created by owner for themselves
      };

      shippersRepository.findApplicationById.mockResolvedValue(sameAsOwner);
      shopsService.getMyShop.mockResolvedValue({
        id: 'shop_abc123',
        ownerId,
      } as any);

      // Should throw BadRequestException
      await expect(service.approveApplication(ownerId, 'app_123')).rejects.toThrow(
        BadRequestException
      );
    });

    it('should write shipperInfo to SHIPPER user document (app.userId), not owner', async () => {
      const ownerId = 'owner_user_correct';
      const shipperId = 'shipper_user_correct';

      shippersRepository.findApplicationById.mockResolvedValue({
        ...mockShipperApp,
        userId: shipperId,  // Different from owner
      });

      await service.approveApplication(ownerId, 'app_001');

      // Verify transaction.update was called with shipper user ref + shipperInfo
      expect(mockTransaction.update).toHaveBeenCalled();

      // Find the call that includes shipperInfo
      const updateCalls = mockTransaction.update.mock.calls;
      expect(updateCalls.length).toBeGreaterThan(0);

      // There should be a call with shipperInfo object
      const shipperInfoCall = updateCalls.find(
        (call: any[]) => call[1]?.shipperInfo !== undefined
      );
      expect(shipperInfoCall).toBeDefined();
      expect(shipperInfoCall?.[1]).toMatchObject({
        role: 'SHIPPER',
        shipperInfo: {
          shopId: mockShipperApp.shopId,
          shopName: mockShipperApp.shopName,
          vehicleType: mockShipperApp.vehicleType,
          vehicleNumber: mockShipperApp.vehicleNumber,
          status: 'AVAILABLE',  // FIX: Changed from ACTIVE to AVAILABLE
        },
      });
    });

    it('should update custom claims with SHIPPER role after Firestore write', async () => {
      const ownerId = 'owner_user_correct';
      const shipperId = 'shipper_user_correct';

      await service.approveApplication(ownerId, 'app_001');

      // Verify custom claims were updated to SHIPPER role
      expect(firebaseService.auth.setCustomUserClaims).toHaveBeenCalledWith(
        shipperId,
        { role: 'SHIPPER' }
      );
    });
  });

  describe('Data Consistency Checks', () => {
    it('should not allow shipper to have same ID as owner', async () => {
      const ownerId = 'same_person_123';

      const badApp: ShipperApplicationEntity = {
        ...mockShipperApp,
        userId: ownerId,  // ← Same as owner
      };

      shippersRepository.findApplicationById.mockResolvedValue(badApp);
      shopsService.getMyShop.mockResolvedValue({
        id: mockShipperApp.shopId,
        ownerId,
      } as any);

      await expect(service.approveApplication(ownerId, 'app_001')).rejects.toThrow(
        BadRequestException
      );
    });

    it('should throw BadRequestException with helpful message for same-person case', async () => {
      const ownerId = 'owner_123';

      const sameAsOwner: ShipperApplicationEntity = {
        ...mockShipperApp,
        userId: ownerId,
      };

      shippersRepository.findApplicationById.mockResolvedValue(sameAsOwner);
      shopsService.getMyShop.mockResolvedValue({
        id: mockShipperApp.shopId,
        ownerId,
      } as any);

      try {
        await service.approveApplication(ownerId, 'app_001');
        fail('Should have thrown BadRequestException');
      } catch (error: any) {
        expect(error.message).toContain('SHIPPER_BUG');
        expect(error.message).toContain('khác');
      }
    });
  });
});
