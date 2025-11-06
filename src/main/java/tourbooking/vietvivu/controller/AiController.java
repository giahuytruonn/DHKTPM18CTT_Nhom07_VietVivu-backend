package tourbooking.vietvivu.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.service.TourRagService;
import tourbooking.vietvivu.service.TourService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AiController {
    TourRagService tourRagService;
    TourService tourService;
    @PostMapping(value = "/chat", produces = "text/event-stream")
    public ApiResponse<Flux<String>> chatWithAi(@RequestBody String userMessage){
        return ApiResponse.<Flux<String>>builder()
                .result(tourRagService.streamSuggestTour(userMessage))
                .build();
    }

    @PostMapping("/reindex")
    public ApiResponse<String> reindexData(){
        List<Tour> tours = tourService.findAllTours();
        tourRagService.indexTourData(tours);
        return ApiResponse.<String>builder()
                .result(tourRagService.indexTourData(tours))
                .build();
    }

}
