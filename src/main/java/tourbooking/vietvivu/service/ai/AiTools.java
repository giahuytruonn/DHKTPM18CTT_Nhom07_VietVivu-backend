package tourbooking.vietvivu.service.ai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.response.ChatResponse;
import tourbooking.vietvivu.dto.response.TourSummary;
import tourbooking.vietvivu.dto.response.TourSummaryArray;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.repository.ImageRepository;
import tourbooking.vietvivu.service.TourService;

@Component
@RequiredArgsConstructor
public class AiTools {
    private final TourService tourService;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ImageRepository imageRepository;

    // Hướng dẫn quy trình đặt Tour
    @Tool(description = "Hướng dẫn người dùng quy trình đặt tour trên website VietViVu")
    public ChatResponse getInstructionToBookingTour() {
        return new ChatResponse(
                """
			✈️ **Hướng dẫn đặt tour trên VietViVu**

			1️⃣ **Tìm kiếm tour:**
			- Vào trang chủ VietViVu (vietvivu.vn).
			- Chọn điểm đến, ngày khởi hành và số lượng khách.

			2️⃣ **Xem chi tiết tour:**
			- Nhấn vào tour mong muốn để xem thông tin chi tiết, lịch trình, giá và chính sách.

			3️⃣ **Đặt tour:**
			- Chọn ngày khởi hành, số lượng người.
			- Nhấn nút **“Đặt ngay”**.

			4️⃣ **Điền thông tin khách hàng:**
			- Họ tên, số điện thoại, email.
			- Ghi chú nếu có yêu cầu đặc biệt.

			5️⃣ **Thanh toán:**
			- Chọn phương thức thanh toán: chuyển khoản, ví điện tử, hoặc tiền mặt tại văn phòng.
			- Sau khi thanh toán, bạn sẽ nhận được **xác nhận đặt tour** qua email.

			6️⃣ **Hoàn tất:**
			- Nhân viên VietViVu sẽ liên hệ xác nhận chi tiết.
			- Bạn chỉ cần chuẩn bị hành lý và tận hưởng chuyến đi!

			📞 **Hỗ trợ:** Hotline 1900-888-555 hoặc email: support@vietvivu.vn
		""");
    }

    @Tool(name = "getGreetingMessage", description = "Chào hỏi người dùng khi họ bắt đầu cuộc trò chuyện")
    public ChatResponse getGreetingMessage() {
        return new ChatResponse(
                """
			👋 Xin chào! Tôi có thể giúp bạn tìm kiếm tour du lịch phù hợp hoặc trả lời các câu hỏi về quy trình đặt, hủy, đổi tour. Bạn cần hỗ trợ gì hôm nay?
			""");
    }

    @Tool(description = "Hướng dẫn người dùng quy trình hủy Tour trên website VietViVu")
    public ChatResponse getInstructionToCancelBookingTour() {
        return new ChatResponse(
                """
		❌ **Hướng dẫn hủy tour trên VietViVu**

		1️⃣ **Đăng nhập tài khoản:**
		- Truy cập website [vietvivu.vn](https://vietvivu.vn)
		- Vào mục **"Tài khoản của tôi" → "Đơn hàng của tôi"**

		2️⃣ **Chọn tour cần hủy:**
		- Chọn tour bạn muốn hủy trong danh sách đặt tour gần đây.
		- Nhấn nút **"Yêu cầu hủy tour"**.

		3️⃣ **Xác nhận lý do hủy:**
		- Chọn lý do hủy (ví dụ: thay đổi kế hoạch, lý do cá nhân,…).
		- Xác nhận gửi yêu cầu.

		4️⃣ **Phí hủy và hoàn tiền:**
		- Nếu hủy **trước ngày khởi hành 7 ngày** → Hoàn **100%**.
		- Hủy **trước 3-6 ngày** → Hoàn **70%**.
		- Hủy **dưới 3 ngày** → Không hoàn tiền (trừ trường hợp bất khả kháng).
		- VietViVu sẽ xử lý hoàn tiền trong **3-5 ngày làm việc**.

		5️⃣ **Liên hệ hỗ trợ:**
		- Hotline: **1900-888-555**
		- Email: **support@vietvivu.vn**
		- Hoặc đến trực tiếp văn phòng VietViVu để được hỗ trợ nhanh nhất.

		ℹ️ *Lưu ý:*
		- Một số tour khuyến mãi hoặc tour đặc biệt có chính sách hủy riêng, vui lòng xem chi tiết trong điều khoản tour của bạn.
	""");
    }

    @Tool(description = "Hướng dẫn người dùng quy trình đổi Tour trên website VietViVu")
    public ChatResponse getInstructionToChangeBookingTour() {
        return new ChatResponse(
                """
		🔄 **Hướng dẫn đổi tour trên VietViVu**

		1️⃣ **Truy cập tài khoản:**
		- Đăng nhập vào website [vietvivu.vn](https://vietvivu.vn)
		- Chọn mục **“Tài khoản của tôi” → “Đơn hàng của tôi”**.

		2️⃣ **Chọn tour muốn đổi:**
		- Trong danh sách các tour đã đặt, chọn tour cần đổi.
		- Nhấn nút **“Yêu cầu đổi tour”**.

		3️⃣ **Chọn tour mới:**
		- Chọn tour hoặc ngày khởi hành mới bạn muốn chuyển sang.
		- Kiểm tra lại **giá tour mới**, lịch trình và chính sách.

		4️⃣ **Chính sách đổi tour:**
		- Đổi **trước ngày khởi hành ít nhất 5 ngày**: miễn phí.
		- Đổi **trước 2–4 ngày**: phụ thu **10% giá tour**.
		- Đổi **dưới 2 ngày**: có thể bị từ chối do tour đã được sắp xếp.
		- Nếu tour mới có giá cao hơn, bạn cần thanh toán phần chênh lệch.

		5️⃣ **Xác nhận đổi tour:**
		- Sau khi gửi yêu cầu, nhân viên VietViVu sẽ liên hệ xác nhận.
		- Bạn sẽ nhận được **email xác nhận đổi tour** sau khi hoàn tất.

		6️⃣ **Hỗ trợ thêm:**
		- Hotline: **1900-888-555**
		- Email: **support@vietvivu.vn**

		ℹ️ *Lưu ý:*
		- Một số tour khuyến mãi hoặc tour theo đoàn có thể **không áp dụng đổi lịch hoặc đổi điểm đến**.
		- Vui lòng đọc kỹ điều khoản cụ thể trong hợp đồng tour trước khi gửi yêu cầu.
	""");
    }

    @Tool(description = "Tìm kiếm và gợi ý tour du lịch phù hợp dựa trên truy vấn của người dùng")
    public TourSummary findTour(String query) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(1).build());

        if (results.isEmpty()) {
            return null; // BeanOutputConverter sẽ parse null thành null
        }

        Document doc = results.getFirst();

        System.out.println(imageRepository.findImageUrlsByTour_TourId(
                doc.getMetadata().get("tourId").toString()));
        return new TourSummary(
                doc.getMetadata().get("tourId").toString(),
                doc.getMetadata().get("title").toString(),
                doc.getMetadata().get("priceAdult").toString(),
                doc.getMetadata().get("priceChild").toString(),
                doc.getMetadata().get("duration").toString(),
                doc.getMetadata().get("imageUrls").toString().split(","));
    }

    @Tool(description = "Gợi ý các tour du lịch hot nhất hiện nay")
    public TourSummaryArray findAllHotTours() {

        List<Tour> tours = tourService.findAllHotTours();

        String summaryId = "hot_tours_01";
        TourSummary[] summaries = new TourSummary[tours.size()];
        for (int i = 0; i < tours.size(); i++) {
            Tour tour = tours.get(i);
            List<String> imageUrls = imageRepository.findImageUrlsByTour_TourId(tour.getTourId());
            summaries[i] = new TourSummary(
                    tour.getTourId(),
                    tour.getTitle(),
                    String.valueOf(tour.getPriceAdult()),
                    String.valueOf(tour.getPriceChild()),
                    (String.valueOf(tour.getDuration())),
                    imageUrls.toArray(new String[0]));
        }
        return new TourSummaryArray(summaryId, summaries);
    }

    @Tool(description = "Trả lời khi không tìm thấy tour phù hợp")
    public ChatResponse getNoTourFoundMessage() {
        return new ChatResponse(
                """
			Xin lỗi, hiện tại tôi không tìm thấy tour du lịch phù hợp với yêu cầu của bạn. Bạn có muốn tôi gợi ý thêm các tour khác không?
			""");
    }

    @Tool(description = "Trả lời khi câu hỏi không thuộc phạm vi hỗ trợ của VietViVu")
    public ChatResponse getOutOfScopeMessage() {
        return new ChatResponse(
                """
			Xin lỗi, tôi chỉ hỗ trợ các vấn đề liên quan đến tour du lịch VietViVu. Vui lòng đặt câu hỏi về du lịch hoặc dịch vụ của chúng tôi.
			""");
    }
}
