package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
}
