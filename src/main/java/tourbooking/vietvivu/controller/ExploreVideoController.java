package tourbooking.vietvivu.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.ExploreVideoRequest;
import tourbooking.vietvivu.dto.response.ExploreVideoResponse;
import tourbooking.vietvivu.service.ExploreVideoService;

@RestController
@RequestMapping("/api/explore")
@RequiredArgsConstructor
public class ExploreVideoController {

    private final ExploreVideoService exploreVideoService;

    @PostMapping("/admin/upload")
    public ResponseEntity<ExploreVideoResponse> uploadVideo(@Valid @RequestBody ExploreVideoRequest videoRequest) {
        return ResponseEntity.ok(exploreVideoService.uploadVideo(videoRequest));
    }

    @PostMapping("/like/{videoId}")
    public ResponseEntity<ExploreVideoResponse> toggleLike(@PathVariable String videoId, @RequestParam boolean isLike) {
        return ResponseEntity.ok(exploreVideoService.toggleLike(videoId, isLike));
    }

    @GetMapping("/videos")
    public ResponseEntity<List<ExploreVideoResponse>> getAllVideos() {
        return ResponseEntity.ok(exploreVideoService.getAllVideos());
    }

    @DeleteMapping("/admin/delete/{videoId}")
    public ResponseEntity<?> deleteVideo(@PathVariable String videoId) {
        exploreVideoService.deleteVideo(videoId);
        return ResponseEntity.ok("Xóa video thành công");
    }

    @PutMapping("/admin/update/{videoId}")
    public ResponseEntity<ExploreVideoResponse> updateVideo(
            @PathVariable String videoId, @Valid @RequestBody ExploreVideoRequest videoRequest) {
        return ResponseEntity.ok(exploreVideoService.updateVideo(videoId, videoRequest));
    }
}
