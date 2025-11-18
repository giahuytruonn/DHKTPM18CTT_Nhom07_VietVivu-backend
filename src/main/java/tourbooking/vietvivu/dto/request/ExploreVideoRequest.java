package tourbooking.vietvivu.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExploreVideoRequest {
    @NotBlank(message = "Title không được để trống")
    private String title;

    private String description;

    @NotBlank(message = "Video URL không được để trống")
    private String videoUrl;
}
