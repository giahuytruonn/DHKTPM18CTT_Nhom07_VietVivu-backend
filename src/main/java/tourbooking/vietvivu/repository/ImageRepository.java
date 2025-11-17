package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import feign.Param;
import tourbooking.vietvivu.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {

    @Query("SELECT i.imageUrl FROM Image i WHERE i.tour.tourId = :tourId")
    List<String> findImageUrlsByTour_TourId(@Param("tourId") String tourId);
}
