/**
 * Firebase Functions Entry Point
 *
 * File này export NestJS app như một Firebase Cloud Function.
 * Sử dụng lazy initialization để optimize cold start.
 */

import { NestFactory } from '@nestjs/core';
import { ExpressAdapter } from '@nestjs/platform-express';
import { onRequest } from 'firebase-functions/v2/https';
import express from 'express';
import { ValidationPipe, INestApplication } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { AppModule } from './app.module';

// Express instance (reused across invocations)
const expressServer = express();

/**
 * Bootstrap NestJS application
 */
const createNestServer = async (expressInstance: express.Express) => {
  const adapter = new ExpressAdapter(expressInstance);
  const app = await NestFactory.create(AppModule, adapter, {
    logger: ['error', 'warn', 'log'],
  });

  // Enable CORS
  app.enableCors({
    origin: true,
    credentials: true,
  });

  // Global validation pipe
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

  // API prefix
  app.setGlobalPrefix('api');

  // Swagger setup
  const config = new DocumentBuilder()
    .setTitle('KTX Delivery API')
    .setDescription('REST API cho ứng dụng giao hàng KTX')
    .setVersion('2.0')
    .addBearerAuth(
      {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'Firebase ID Token',
        description: 'Enter Firebase ID Token (get from Firebase Auth client SDK)',
      },
      'firebase-auth',
    )
    .addTag('Auth', 'Authentication & Authorization')
    .addTag('Admin - Categories', 'Admin: Category management')
    .addTag('Admin - Users', 'Admin: User management')
    .addTag('Admin - Payouts', 'Admin: Payout approval')
    .addTag('Admin - Shops', 'Admin: Shop management')
    .addTag('Admin - Dashboard', 'Admin: Statistics')
    .addTag('Categories', 'Public: Product categories')
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api/docs', app, document, {
    swaggerOptions: {
      persistAuthorization: true, // Remember token in browser
      tagsSorter: 'alpha',
      operationsSorter: 'alpha',
    },
  });

  await app.init();
  return app;
};

// Lazy initialization
let server: INestApplication | null = null;

/**
 * Main API endpoint
 *
 * URL: https://{region}-{project}.cloudfunctions.net/api
 * Local: http://127.0.0.1:5001/{project}/asia-southeast1/api
 */
export const api = onRequest(
  {
    region: 'asia-southeast1',
    memory: '512MiB',
    timeoutSeconds: 60,
    minInstances: 0,
    maxInstances: 100,
  },
  async (req, res) => {
    if (!server) {
      server = await createNestServer(expressServer);
    }
    expressServer(req, res);
  },
);
