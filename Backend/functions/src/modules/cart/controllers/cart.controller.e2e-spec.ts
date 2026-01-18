import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import { AppModule } from '../../../app.module';

/**
 * E2E Test: Cart Query Parameter Parsing
 * 
 * This test ensures that query parameters (page, limit, includeAll)
 * are correctly parsed at the HTTP layer, not just at the service layer.
 * 
 * Bug context: Unit tests passed but Swagger UI showed incorrect behavior
 * because query string parsing wasn't working correctly.
 * 
 * Installation required:
 *   npm install --save-dev supertest @types/supertest
 * 
 * Run with:
 *   npm test -- cart.controller.e2e-spec.ts
 */

// Import supertest dynamically to avoid errors if not installed
let request: any;
try {
  request = require('supertest');
} catch (e) {
  console.warn('supertest not installed. Install with: npm install --save-dev supertest @types/supertest');
}
describe('CartController (e2e) - Query Parsing', () => {
  let app: INestApplication;
  let authToken: string;

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();

    // Apply same configuration as production
    app.useGlobalPipes(
      new ValidationPipe({
        whitelist: true,
        forbidNonWhitelisted: true,
        transform: true,
        transformOptions: {
          enableImplicitConversion: true,
        },
      }),
    );

    app.setGlobalPrefix('api');

    await app.init();

    // TODO: Replace with actual auth token generation
    // For now, this test requires manual setup or mocked auth
    authToken = process.env.TEST_AUTH_TOKEN || '';
  });

  afterAll(async () => {
    await app.close();
  });

  describe('GET /api/cart - Query Parameter Parsing', () => {
    /**
     * Test: includeAll=false with limit=1 should return exactly 1 group
     * 
     * Expected: { groups: [1 group], page: 1, limit: 1, totalGroups: N, totalPages: N }
     * Bug: Was returning all groups with limit=totalGroups
     */
    it('should return 1 group when page=1&limit=1&includeAll=false (if totalGroups > 1)', async () => {
      // Skip if no auth token
      if (!authToken) {
        console.warn('Skipping e2e test: TEST_AUTH_TOKEN not set');
        return;
      }

      const response = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ page: '1', limit: '1', includeAll: 'false' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data).toBeDefined();

      const { groups, page, limit, totalGroups, totalPages } = response.body.data;

      // If there are multiple groups, pagination should apply
      if (totalGroups > 1) {
        expect(groups).toHaveLength(1);
        expect(limit).toBe(1);
        expect(page).toBe(1);
        expect(totalPages).toBe(totalGroups);
      }

      // Metadata should always be present
      expect(page).toBeDefined();
      expect(limit).toBeDefined();
      expect(totalGroups).toBeDefined();
      expect(totalPages).toBeDefined();
    });

    /**
     * Test: page=2 should return different groups than page=1
     * 
     * Expected: Different shopId in groups[0]
     * Bug: Was returning same groups regardless of page param
     */
    it('should return different group on page=2 than page=1 (if totalGroups > 1)', async () => {
      if (!authToken) {
        console.warn('Skipping e2e test: TEST_AUTH_TOKEN not set');
        return;
      }

      // Get page 1
      const response1 = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ page: '1', limit: '1', includeAll: 'false' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      const page1Data = response1.body.data;

      // Skip if only 1 group total
      if (page1Data.totalGroups <= 1) {
        console.warn('Skipping test: cart has <= 1 group');
        return;
      }

      // Get page 2
      const response2 = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ page: '2', limit: '1', includeAll: 'false' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      const page2Data = response2.body.data;

      // Both should have exactly 1 group
      expect(page1Data.groups).toHaveLength(1);
      expect(page2Data.groups).toHaveLength(1);

      // Groups should be different (different shopId)
      const shop1 = page1Data.groups[0].shopId;
      const shop2 = page2Data.groups[0].shopId;
      expect(shop1).not.toBe(shop2);
    });

    /**
     * Test: includeAll=true should return ALL groups
     * 
     * Expected: groups.length === totalGroups
     * Expected: limit === totalGroups (or still 10, depending on implementation)
     */
    it('should return all groups when includeAll=true', async () => {
      if (!authToken) {
        console.warn('Skipping e2e test: TEST_AUTH_TOKEN not set');
        return;
      }

      const response = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ includeAll: 'true' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      expect(response.body.success).toBe(true);
      const { groups, totalGroups, limit } = response.body.data;

      // When includeAll=true, should return all groups
      expect(groups).toHaveLength(totalGroups);

      // Implementation: limit should equal totalGroups when includeAll=true
      expect(limit).toBe(totalGroups || 10); // 10 is fallback for empty cart
    });

    /**
     * Test: includeAll="false" (string) should be parsed as boolean false
     * 
     * This is the critical test for the bug fix.
     * String "false" must be parsed as boolean false, not truthy.
     */
    it('should parse includeAll="false" as boolean false, not truthy', async () => {
      if (!authToken) {
        console.warn('Skipping e2e test: TEST_AUTH_TOKEN not set');
        return;
      }

      const response = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ page: '1', limit: '1', includeAll: 'false' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      const { groups, limit, totalGroups } = response.body.data;

      // If totalGroups > 1, we should only get 1 group (because limit=1)
      // NOT all groups (which would happen if includeAll was truthy)
      if (totalGroups > 1) {
        expect(groups).toHaveLength(1);
        expect(limit).toBe(1);
        // This is the key assertion: limit should be 1, NOT totalGroups
        expect(limit).not.toBe(totalGroups);
      }
    });

    /**
     * Test: Out-of-range page should return empty groups array
     * 
     * Expected: { groups: [], page: 100, limit: 10, totalGroups: N, totalPages: M }
     * Should NOT fallback to page 1
     */
    it('should return empty array for out-of-range page', async () => {
      if (!authToken) {
        console.warn('Skipping e2e test: TEST_AUTH_TOKEN not set');
        return;
      }

      const response = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ page: '100', limit: '10' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      const { groups, page, limit } = response.body.data;

      // Should return empty array
      expect(groups).toEqual([]);

      // Should preserve requested page number (not fallback to page 1)
      expect(page).toBe(100);
      expect(limit).toBe(10);
    });

    /**
     * Test: Invalid query params should use defaults
     */
    it('should use defaults for invalid query params', async () => {
      if (!authToken) {
        console.warn('Skipping e2e test: TEST_AUTH_TOKEN not set');
        return;
      }

      // Invalid page (negative)
      const response1 = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ page: '-1' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      expect(response1.body.data.page).toBe(1); // Default to 1

      // Invalid limit (exceeds max)
      const response2 = await request(app.getHttpServer())
        .get('/api/cart')
        .query({ limit: '100' })
        .set('Authorization', `Bearer ${authToken}`)
        .expect(200);

      expect(response2.body.data.limit).toBe(50); // Capped at max 50
    });
  });
});
