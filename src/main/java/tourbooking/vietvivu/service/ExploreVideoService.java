package tourbooking.vietvivu.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.ExploreVideoRequest;
import tourbooking.vietvivu.dto.response.ExploreVideoResponse;
import tourbooking.vietvivu.entity.ExploreVideo;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.repository.ExploreVideoRepository;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ExploreVideoService {

    private final ExploreVideoRepository exploreVideoRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    public ExploreVideoResponse uploadVideo(String username, ExploreVideoRequest req) {
        User uploader =
                userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        ExploreVideo video = new ExploreVideo();
        video.setTitle(req.getTitle());
        video.setDescription(req.getDescription());
        video.setVideoUrl(req.getVideoUrl());
        video.setUploader(uploader);
        video.setUploadedAt(LocalDateTime.now());
        video.setApproved(false); // chờ duyệt

        ExploreVideo saved = exploreVideoRepository.save(video);
        return toResponse(saved);
    }

    public List<ExploreVideoResponse> getApprovedVideos() {
        return exploreVideoRepository.findByApprovedTrueOrderByUploadedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public List<ExploreVideoResponse> getPendingVideos() {
        return exploreVideoRepository.findByApprovedFalseOrderByUploadedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExploreVideoResponse approveVideo(String videoId) {
        ExploreVideo video =
                exploreVideoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video không tồn tại"));
        video.setApproved(true);
        return toResponse(exploreVideoRepository.save(video));
    }

    public void deleteVideo(String videoId) {
        exploreVideoRepository.deleteById(videoId);
    }

    public ExploreVideoResponse updateVideo(String videoId, ExploreVideoRequest req) {
        ExploreVideo video =
                exploreVideoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video không tồn tại"));

        video.setTitle(req.getTitle());
        video.setDescription(req.getDescription());
        video.setVideoUrl(req.getVideoUrl());

        return toResponse(exploreVideoRepository.save(video));
    }

    private ExploreVideoResponse toResponse(ExploreVideo video) {
        return new ExploreVideoResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getVideoUrl(),
                video.getUploader().getUsername(),
                video.getApproved(),
                video.getUploadedAt());
    }
}
