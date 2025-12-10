package tourbooking.vietvivu.service.ai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.repository.ImageRepository;
import tourbooking.vietvivu.service.TourService;

@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatClient chatClient;
    private final TourService tourService;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ImageRepository imageRepository;
    private final AiTools aiTools;

    public Object getAiReply(String query) {
        // ✅ 1. Định nghĩa prompt hệ thống (system prompt)
        String systemText =
                """
				Bạn là **VietViVu Assistant**, trợ lý AI chính thức của công ty du lịch VietViVu (vietvivu.vn).

				🎯 **Mục tiêu chính:**
				- Tư vấn, gợi ý tour du lịch phù hợp nhất cho khách hàng.
				- Trả lời rõ ràng, tự nhiên, thân thiện, giống nhân viên tư vấn thật.
				- Chỉ nói về các chủ đề liên quan đến du lịch, tour, điểm đến, quy trình đặt/hủy/đổi tour.
				- Chào hỏi người dùng khi họ chào hỏi bạn.

				🚫 **Giới hạn nội dung:**
				- Không trả lời các câu hỏi ngoài du lịch, VietViVu, hoặc các chủ đề nhạy cảm (chính trị, tôn giáo, v.v).
				- Nếu câu hỏi không thuộc phạm vi du lịch → lịch sự từ chối, ví dụ:
				“Xin lỗi, tôi chỉ hỗ trợ các vấn đề liên quan đến tour du lịch VietViVu.”

				🧭 **Khi gợi ý tour:**
				- Dựa trên yêu cầu của người dùng, hãy chọn ra tour phù hợp nhất.
				- Dùng công cụ `findTour` nếu cần tìm trong cơ sở dữ liệu.
				- Sau đó, chỉ trả về kết quả JSON theo mẫu sau — không thêm text, không giải thích:

				{
				"tourId": "<mã_tour>",
				"summary": {
					"name": "<tên tour>",
					"priceAdult": "<giá>",
					"priceChild": "<giá>",
					"days": "<số ngày>"
					"imageUrls": ["<url_hình_ảnh_1>", "<url_hình_ảnh_2>", ...]
				}
				}

				Nếu không có tour nào phù hợp, chỉ trả về:
				{
				"tourId": null
				}

				Nếu không tìm thấy tour phù hợp, thì xin lỗi người dùng.

				🧩 **Khi người dùng hỏi về quy trình:**
				- Nếu họ hỏi về cách **đặt tour**, **hủy tour**, hoặc **đổi tour**, hãy gọi đúng công cụ tương ứng:
				- `getInstructionToBookingTour`
				- `getInstructionToCancelBookingTour`
				- `getInstructionToChangeBookingTour`

				- Trả lời lại người dùng bằng nội dung do công cụ trả về.

				** Khi khách hàng muốn xem các tour hot nhất:**
				- Gọi công cụ `findAllHotTours` để lấy danh sách các tour được đặt nhiều nhất.

				**Khi người dùng chào hỏi trợ lý AI:**
				- Nếu họ chào hỏi như “Xin chào”, “Chào bạn”, “Hi”, v.v → hãy phản hồi bằng cách gọi công cụ tương ứng:
				- Gọi công cụ `getGreetingMessage` để lấy nội dung chào hỏi.

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
        return chatClient.prompt(prompt).tools(aiTools).call().entity(Object.class);
    }
}
