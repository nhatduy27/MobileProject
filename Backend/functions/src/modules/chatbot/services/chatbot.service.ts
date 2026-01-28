import { Injectable, Inject, OnModuleInit, Logger } from '@nestjs/common';
import { GoogleGenAI, GenerateContentResponse } from '@google/genai';
import { Firestore } from 'firebase-admin/firestore';
import { ConfigService } from '../../../core/config/config.service';
import { globalCache, CACHE_TTL } from '../../../shared/utils';

interface FaqEntry {
  id: string;
  question: string;
  answer: string;
  category: string;
}

export interface ChatResponse {
  answer: string;
  sources?: string[];
  confidence: 'high' | 'medium' | 'low';
  rateLimited?: boolean;
  waitTime?: number;
}

// Rate limit config: 3 requests per 60 seconds = 20 seconds between requests
const RATE_LIMIT_WINDOW_MS = 60 * 1000; // 1 minute
const MAX_REQUESTS_PER_WINDOW = 3;
const MIN_INTERVAL_MS = RATE_LIMIT_WINDOW_MS / MAX_REQUESTS_PER_WINDOW; // 20 seconds

/**
 * Chatbot Service
 *
 * AI-powered customer support using Google Gemini.
 * Features:
 * - FAQ-based context for accurate answers
 * - Vietnamese language support
 * - Fallback to general knowledge when FAQ doesn't cover topic
 * - Rate limiting: max 3 requests per minute per user
 */
@Injectable()
export class ChatbotService implements OnModuleInit {
  private readonly logger = new Logger(ChatbotService.name);
  private genai: GoogleGenAI | null = null;
  private readonly FAQ_CACHE_KEY = 'chatbot:faq:context';

  // Rate limiting: track last request time per user
  private userLastRequestTime: Map<string, number> = new Map();

  constructor(
    @Inject('FIRESTORE') private readonly firestore: Firestore,
    private readonly configService: ConfigService,
  ) {}

  async onModuleInit() {
    const apiKey = this.configService.get('GEMINI_API_KEY');
    if (apiKey) {
      this.genai = new GoogleGenAI({ apiKey });
      this.logger.log('✅ Gemini AI initialized');
    } else {
      this.logger.warn('⚠️ GEMINI_API_KEY not configured - chatbot disabled');
    }

    // Cleanup old entries every 5 minutes
    setInterval(() => this.cleanupRateLimitMap(), 5 * 60 * 1000);
  }

  /**
   * Cleanup stale entries from rate limit map
   */
  private cleanupRateLimitMap(): void {
    const now = Date.now();
    const staleThreshold = 10 * 60 * 1000; // 10 minutes

    for (const [userId, lastTime] of this.userLastRequestTime.entries()) {
      if (now - lastTime > staleThreshold) {
        this.userLastRequestTime.delete(userId);
      }
    }
  }

  /**
   * Check and apply rate limiting for a user
   * Returns wait time in seconds if rate limited, or 0 if allowed
   */
  private checkRateLimit(userId: string): number {
    const now = Date.now();
    const lastRequestTime = this.userLastRequestTime.get(userId) || 0;
    const timeSinceLastRequest = now - lastRequestTime;

    if (timeSinceLastRequest < MIN_INTERVAL_MS) {
      // User needs to wait
      const waitTimeMs = MIN_INTERVAL_MS - timeSinceLastRequest;
      return Math.ceil(waitTimeMs / 1000);
    }

    return 0;
  }

  /**
   * Process a chat message and generate a response
   * Rate limited to 3 requests per minute per user
   */
  async chat(userId: string, message: string): Promise<ChatResponse> {
    if (!this.genai) {
      return {
        answer:
          'Xin lỗi, tính năng chatbot đang được bảo trì. Vui lòng liên hệ hotline để được hỗ trợ.',
        confidence: 'low',
      };
    }

    // Check rate limit
    const waitTimeSeconds = this.checkRateLimit(userId);
    if (waitTimeSeconds > 0) {
      this.logger.warn(`Rate limit: User ${userId} must wait ${waitTimeSeconds}s`);
      return {
        answer: `Bạn đang gửi tin nhắn quá nhanh. Vui lòng đợi ${waitTimeSeconds} giây trước khi gửi tiếp.`,
        confidence: 'low',
        rateLimited: true,
        waitTime: waitTimeSeconds,
      };
    }

    // Update last request time
    this.userLastRequestTime.set(userId, Date.now());

    try {
      // Get FAQ context
      const faqContext = await this.getFaqContext();

      // Build system prompt
      const systemPrompt = this.buildSystemPrompt(faqContext);

      // Generate response
      const model = this.genai.models;
      const result: GenerateContentResponse = await model.generateContent({
        model: 'gemini-2.5-flash-lite',
        contents: [
          {
            role: 'user',
            parts: [{ text: `${systemPrompt}\n\nKhách hàng hỏi: ${message}` }],
          },
        ],
      });

      const responseText =
        result.text || 'Xin lỗi, tôi không thể trả lời câu hỏi này. Vui lòng thử lại.';

      return {
        answer: responseText,
        confidence: 'high',
      };
    } catch (error) {
      this.logger.error('Chat error:', error);
      return {
        answer: 'Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.',
        confidence: 'low',
      };
    }
  }

  /**
   * Get FAQ entries as context for the AI
   */
  private async getFaqContext(): Promise<string> {
    // Check cache first
    const cached = globalCache.get<string>(this.FAQ_CACHE_KEY);
    if (cached) {
      return cached;
    }

    // Fetch from Firestore
    const snapshot = await this.firestore.collection('faqs').limit(100).get();

    const faqs: FaqEntry[] = snapshot.docs.map((doc) => ({
      id: doc.id,
      question: doc.data().question,
      answer: doc.data().answer,
      category: doc.data().category || 'general',
    }));

    // Format as context
    const context = faqs.map((faq) => `Q: ${faq.question}\nA: ${faq.answer}`).join('\n\n');

    // Cache for 5 minutes
    globalCache.set(this.FAQ_CACHE_KEY, context, CACHE_TTL.MENU * 2.5);

    return context;
  }

  /**
   * Build system prompt with FAQ context
   */
  private buildSystemPrompt(faqContext: string): string {
    return `Bạn là trợ lý AI của KTX Delivery - ứng dụng giao đồ ăn trong ký túc xá.

NHIỆM VỤ:
- Trả lời câu hỏi của khách hàng về dịch vụ một cách thân thiện và ngắn gọn
- Ưu tiên sử dụng thông tin từ FAQ bên dưới nếu có liên quan
- Nếu không tìm thấy câu trả lời trong FAQ, hãy trả lời dựa trên kiến thức chung về dịch vụ giao đồ ăn
- Luôn trả lời bằng tiếng Việt
- Giữ câu trả lời ngắn gọn, dễ hiểu

FAQ DATABASE:
${faqContext}

LƯU Ý:
- Nếu hỏi về đơn hàng cụ thể, hướng dẫn khách xem trong mục "Đơn hàng của tôi"
- Nếu hỏi về vấn đề kỹ thuật phức tạp, đề nghị liên hệ hotline
- Không bịa đặt thông tin về giá cả hay khuyến mãi cụ thể`;
  }
}
