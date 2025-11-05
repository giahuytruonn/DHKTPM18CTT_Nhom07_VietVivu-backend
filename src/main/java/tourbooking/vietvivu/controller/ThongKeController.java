package tourbooking.vietvivu.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.service.ThongKeService;

import java.util.Map;

@RestController
@RequestMapping("/statistical")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ThongKeController {
    ThongKeService thongKeService;

    @GetMapping
    ApiResponse<Map<String, Integer>> getTourBookingCounts() {
        return ApiResponse.<Map<String, Integer>>builder()
                .result(thongKeService.getTourBookingCounts())
                .build();
    }


}
