import { Injectable, Logger } from '@nestjs/common';
import { EventBusPort } from './event-bus.port';

/**
 * In-Memory Event Bus Adapter
 * 
 * Simple in-memory event bus implementation that logs events.
 * This is a stub implementation - replace with RabbitMQ, Kafka, etc. in production.
 */
@Injectable()
export class InMemoryEventBusAdapter extends EventBusPort {
  private readonly logger = new Logger(InMemoryEventBusAdapter.name);

  async publish<T>(eventName: string, payload: T): Promise<void> {
    // TODO: Replace with actual message queue implementation
    // Example for RabbitMQ:
    // await this.amqpConnection.publish(
    //   'events',
    //   eventName,
    //   payload
    // );

    this.logger.log(
      `[EVENT] ${eventName}: ${JSON.stringify(payload, null, 2)}`,
    );
  }
}
