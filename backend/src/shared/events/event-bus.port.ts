/**
 * Event Bus Port (Abstraction)
 * 
 * This abstract class defines the contract for publishing events.
 * Implementations can use in-memory, RabbitMQ, Kafka, AWS SQS, etc.
 */
export abstract class EventBusPort {
  /**
   * Publish an event to the event bus
   * @param eventName - Name of the event
   * @param payload - Event payload data
   */
  abstract publish<T>(eventName: string, payload: T): Promise<void>;
}
