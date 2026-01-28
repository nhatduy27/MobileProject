import { Controller, Post, Body, UseGuards, Req } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiBody } from '@nestjs/swagger';
import { ChatbotService, ChatResponse } from '../services/chatbot.service';
import { AuthGuard } from '../../../core/guards/auth.guard';

/**
 * Chatbot Controller
 *
 * AI-powered customer support endpoints
 */
@ApiTags('Chatbot')
@Controller('chatbot')
export class ChatbotController {
  constructor(private readonly chatbotService: ChatbotService) {}

  /**
   * POST /chatbot/message
   * Send a message to the AI chatbot
   */
  @Post('message')
  @UseGuards(AuthGuard)
  @ApiBearerAuth('firebase-auth')
  @ApiOperation({
    summary: 'Gửi tin nhắn cho chatbot',
    description: 'AI chatbot sẽ trả lời câu hỏi về dịch vụ KTX Delivery',
  })
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        message: {
          type: 'string',
          example: 'Làm sao để hủy đơn hàng?',
        },
      },
      required: ['message'],
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Phản hồi từ chatbot',
    schema: {
      example: {
        success: true,
        data: {
          answer:
            'Để hủy đơn hàng, bạn vào mục "Đơn hàng của tôi", chọn đơn cần hủy và nhấn nút "Hủy đơn". Lưu ý: Chỉ có thể hủy đơn khi đơn chưa được shop xác nhận.',
          confidence: 'high',
        },
      },
    },
  })
  async sendMessage(@Req() req: any, @Body() body: { message: string }): Promise<ChatResponse> {
    const response = await this.chatbotService.chat(req.user.uid, body.message);
    return response;
  }

  /**
   * POST /chatbot/quick-reply
   * Get quick reply suggestions
   */
  @Post('quick-replies')
  @ApiOperation({
    summary: 'Lấy danh sách câu hỏi gợi ý',
    description: 'Trả về các câu hỏi phổ biến để khách hàng chọn nhanh',
  })
  @ApiResponse({
    status: 200,
    description: 'Danh sách câu hỏi gợi ý',
    schema: {
      example: {
        success: true,
        data: {
          quickReplies: [
            'Làm sao để hủy đơn hàng?',
            'Thời gian giao hàng là bao lâu?',
            'Phí ship được tính như thế nào?',
            'Làm sao để liên hệ shop?',
            'Tôi muốn đổi địa chỉ giao hàng',
          ],
        },
      },
    },
  })
  async getQuickReplies() {
    return {
      quickReplies: [
        'Làm sao để hủy đơn hàng?',
        'Thời gian giao hàng là bao lâu?',
        'Phí ship được tính như thế nào?',
        'Làm sao để liên hệ shop?',
        'Tôi muốn đổi địa chỉ giao hàng',
        'Thanh toán online có an toàn không?',
        'Tôi muốn đăng ký làm shipper',
        'Cách sử dụng mã giảm giá?',
      ],
    };
  }
}
