package tourbooking.vietvivu.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationResponse<T> {
    private List<T> items;
    private int currentPage;
    private int pageSizes;
    private int totalItems;
    private int totalPages;
}
