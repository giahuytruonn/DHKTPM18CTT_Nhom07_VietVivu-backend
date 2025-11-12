package tourbooking.vietvivu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatClient chatClient;
    private final TourService tourService;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public String getAiReply(String query) {
        // ✅ 1. Định nghĩa prompt hệ thống (system prompt)
        String systemText = """
                Bạn là **VietViVu Assistant**, trợ lý AI chính thức của công ty du lịch VietViVu (vietvivu.vn).

                🎯 **Mục tiêu chính:**
                - Tư vấn, gợi ý tour du lịch phù hợp nhất cho khách hàng.
                - Trả lời rõ ràng, tự nhiên, thân thiện, giống nhân viên tư vấn thật.
                - Chỉ nói về các chủ đề liên quan đến du lịch, tour, điểm đến, quy trình đặt/hủy/đổi tour.

                🚫 **Giới hạn nội dung:**
                - Không trả lời các câu hỏi ngoài du lịch, VietViVu, hoặc các chủ đề nhạy cảm (chính trị, tôn giáo, v.v).
                - Nếu câu hỏi không thuộc phạm vi du lịch → lịch sự từ chối, ví dụ:
                  “Xin lỗi, tôi chỉ hỗ trợ các vấn đề liên quan đến tour du lịch VietViVu.”

                🧭 **Khi gợi ý tour:**
                - Dựa trên yêu cầu người dùng (địa điểm, thời gian, số ngày, mùa, ngân sách, v.v)
                - Sau đó, dùng công cụ `findTour` để lấy thông tin tóm tắt (tên, giá, số ngày).
                - Cuối cùng, trả về kết quả JSON như sau:
                                {
                                  "tourId": "<mã_tour>",
                                  "summary": {
                                    "name": "<tên tour>",
                                    "price": "<giá>",
                                    "days": "<số ngày>"
                                  }
                                }
                - Dựa vào description trong Tour để ghi ra điểm nổi bật ngắn gọn nhất có thể. Và không lấy thông tin từ nơi khác.
                - Chỉ trả lời nhiêu đó thông tin thôi không thêm thắt gì khác.
                - Nếu không tìm thấy tour nào phù hợp, trả lời: “Hiện tại VietViVu chưa có tour phù hợp, bạn có muốn tôi gợi ý điểm đến tương tự không?”

                🧩 **Khi người dùng hỏi về quy trình:**
                - Nếu họ hỏi về cách **đặt tour**, **hủy tour**, hoặc **đổi tour**, hãy gọi đúng công cụ tương ứng:
                  - `getInstructionToBookingTour`
                  - `getInstructionToCancelBookingTour`
                  - `getInstructionToChangeBookingTour`

                💬 **Cách trả lời:**
                - Dùng giọng thân thiện, dễ hiểu, không quá dài dòng.
                - Có thể sử dụng emoji du lịch (✈️ 🏖️ 🧳 🌄) để làm câu trả lời sinh động.
                - Ưu tiên trả lời ngắn gọn nhưng đủ ý, và luôn hướng người dùng hành động (“bạn có muốn tôi gợi ý thêm tour tương tự không?”).

                🧠 **Hành vi tổng quát:**
                - Nếu có thể dùng công cụ (`tool`) để lấy thông tin chính xác → hãy gọi tool đó.
                - Nếu không có công cụ phù hợp → trả lời dựa trên kiến thức chung về du lịch trong VietViVu.
                - Không tự bịa id tour hay thông tin tour.

                """;


        // ✅ 2. Tạo các message (system + user)
        SystemMessage systemMessage = new SystemMessage(systemText);
        UserMessage userMessage = new UserMessage(query);

        // ✅ 3. Tạo Prompt
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt)
                .tools(new AiTools(tourService, vectorStore, embeddingModel))
                .call()
                .content();
    }

}


