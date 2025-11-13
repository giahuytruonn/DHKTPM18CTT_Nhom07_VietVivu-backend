package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tourbooking.vietvivu.entity.ExploreVideo;

public interface ExploreVideoRepository extends JpaRepository<ExploreVideo, String> {
    List<ExploreVideo> findByApprovedTrueOrderByUploadedAtDesc();

    List<ExploreVideo> findByTour_TourIdAndApprovedTrueOrderByUploadedAtDesc(String tourId);

    List<ExploreVideo> findByApprovedFalseOrderByUploadedAtDesc();
}
