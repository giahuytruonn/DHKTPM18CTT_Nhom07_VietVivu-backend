package tourbooking.vietvivu.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExploreVideoResponse {
    private String id;
    private String title;
    private String description;
    private String videoUrl;
    private String uploaderUsername;
    private Boolean approved;
    private LocalDateTime uploadedAt;
}
