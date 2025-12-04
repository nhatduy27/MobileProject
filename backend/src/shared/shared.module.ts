import { Global, Module } from '@nestjs/common';
import { CachePort } from './cache/cache.port';
import { InMemoryCacheAdapter } from './cache/in-memory-cache.adapter';
import { NotificationPort } from './notifications/notification.port';
import { FcmNotificationAdapter } from './notifications/fcm-notification.adapter';
import { EventBusPort } from './events/event-bus.port';
import { InMemoryEventBusAdapter } from './events/in-memory-event-bus.adapter';

/**
 * Shared Module
 * 
 * Provides shared technical services using Dependency Inversion.
 * All services are defined as abstract ports and bound to concrete adapters.
 * 
 * This module is global, so its exports are available throughout the app.
 */
@Global()
@Module({
  providers: [
    // Cache service - bind abstract port to concrete adapter
    {
      provide: CachePort,
      useClass: InMemoryCacheAdapter,
    },
    // Notification service - bind abstract port to concrete adapter
    {
      provide: NotificationPort,
      useClass: FcmNotificationAdapter,
    },
    // Event bus service - bind abstract port to concrete adapter
    {
      provide: EventBusPort,
      useClass: InMemoryEventBusAdapter,
    },
  ],
  exports: [CachePort, NotificationPort, EventBusPort],
})
export class SharedModule {}
