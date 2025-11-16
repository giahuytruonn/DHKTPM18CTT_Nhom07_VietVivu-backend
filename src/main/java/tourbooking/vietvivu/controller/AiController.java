package tourbooking.vietvivu.controller;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.service.ai.AiService;
import tourbooking.vietvivu.service.TourRagService;
import tourbooking.vietvivu.service.TourService;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AiController {
    TourRagService tourRagService;
    TourService tourService;
    AiService aiService;

    @PostMapping(value = "/chat")
    public ApiResponse<Object> chat(@RequestBody Map<String, String> payload) {
        String query = payload.get("message");
        Object response = aiService.getAiReply(query);
        System.out.println(response);
        return ApiResponse.<Object>builder()
                .result(response)
                .build();

    }

    @PostMapping("/reindex")
    public ApiResponse<String> reindexData() {
        List<Tour> tours = tourService.findAllTours();
        tourRagService.indexTourData(tours);
        return ApiResponse.<String>builder()
                .result(tourRagService.indexTourData(tours))
                .build();
    }

}
