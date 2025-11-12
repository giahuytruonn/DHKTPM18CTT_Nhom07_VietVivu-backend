package tourbooking.vietvivu.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import tourbooking.vietvivu.entity.Tour;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AiTools {
    private final TourService tourService;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    //Hướng dẫn quy trình đặt Tour
    @Tool(description = "Hướng dẫn người dùng quy trình đặt tour trên website VietViVu")
    public String getInstructionToBookingTour() {
        return """
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
        """;
    }

    @Tool(description = "Hướng dẫn người dùng quy trình hủy Tour trên website VietViVu")
    public String getInstructionToCancelBookingTour() {
        return """
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
    """;
    }

    @Tool(description = "Hướng dẫn người dùng quy trình đổi Tour trên website VietViVu")
    public String getInstructionToChangeBookingTour() {
        return """
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
    """;
    }

    @Tool(description = "Tìm thông tin tóm tắt của tour dựa trên mã tourId")
    public Map<String, Object> findTour(String tourId) {
        return tourService.findTourSummaryById(tourId);
    }







}
