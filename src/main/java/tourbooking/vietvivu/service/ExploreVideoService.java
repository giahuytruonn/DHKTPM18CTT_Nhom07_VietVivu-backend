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
import tourbooking.vietvivu.repository.ExploreVideoRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ExploreVideoService {

    private final ExploreVideoRepository exploreVideoRepository;

    public ExploreVideoResponse uploadVideo(ExploreVideoRequest req) {
        ExploreVideo video = new ExploreVideo();
        video.setTitle(req.getTitle());
        video.setDescription(req.getDescription());
        video.setVideoUrl(req.getVideoUrl());
        video.setTourId(req.getTourId());
        video.setLikeCount(0);
        video.setUploadedAt(LocalDateTime.now());

        // Không set approved nữa vì mặc định hiển thị

        ExploreVideo saved = exploreVideoRepository.save(video);
        return toResponse(saved);
    }

    public ExploreVideoResponse toggleLike(String videoId, boolean isLike) {
        ExploreVideo video =
                exploreVideoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video không tồn tại"));

        int currentLikes = video.getLikeCount() == null ? 0 : video.getLikeCount();
        if (isLike) {
            video.setLikeCount(currentLikes + 1);
        } else {
            video.setLikeCount(Math.max(0, currentLikes - 1));
        }

        return toResponse(exploreVideoRepository.save(video));
    }

    public List<ExploreVideoResponse> getAllVideos() {
        return exploreVideoRepository.findAll().stream()
                .sorted((v1, v2) -> v2.getUploadedAt().compareTo(v1.getUploadedAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
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
        video.setTourId(req.getTourId());

        return toResponse(exploreVideoRepository.save(video));
    }

    private ExploreVideoResponse toResponse(ExploreVideo video) {
        return new ExploreVideoResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getVideoUrl(),
                "Admin",
                video.getUploadedAt(),
                video.getLikeCount(),
                video.getTourId());
    }
}
