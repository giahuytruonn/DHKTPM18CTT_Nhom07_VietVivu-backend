package tourbooking.vietvivu.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PostMapping("/upload")
    public ResponseEntity<ExploreVideoResponse> uploadVideo(
            @Valid @RequestBody ExploreVideoRequest videoRequest, @AuthenticationPrincipal Jwt principal) {
        System.out.println("Claims trong token: " + principal.getClaims());
        String username = principal.getClaimAsString("user_name");
        System.out.println("Username lấy từ token: " + username);

        ExploreVideoResponse response = exploreVideoService.uploadVideo(username, videoRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/videos")
    public ResponseEntity<List<ExploreVideoResponse>> getApprovedVideos() {
        List<ExploreVideoResponse> videos = exploreVideoService.getApprovedVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<List<ExploreVideoResponse>> getPendingVideos() {
        List<ExploreVideoResponse> pending = exploreVideoService.getPendingVideos();
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/admin/approve/{videoId}")
    public ResponseEntity<ExploreVideoResponse> approveVideo(@PathVariable String videoId) {
        ExploreVideoResponse approvedVideo = exploreVideoService.approveVideo(videoId);
        return ResponseEntity.ok(approvedVideo);
    }

    @DeleteMapping("/admin/delete/{videoId}")
    public ResponseEntity<?> deleteVideo(@PathVariable String videoId) {
        exploreVideoService.deleteVideo(videoId);
        return ResponseEntity.ok("Xóa video thành công");
    }

    @PutMapping("/admin/update/{videoId}")
    public ResponseEntity<ExploreVideoResponse> updateVideo(
            @PathVariable String videoId, @Valid @RequestBody ExploreVideoRequest videoRequest) {
        ExploreVideoResponse updatedVideo = exploreVideoService.updateVideo(videoId, videoRequest);
        return ResponseEntity.ok(updatedVideo);
    }
}
