package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.ExploreVideo;

@Repository
public interface ExploreVideoRepository extends JpaRepository<ExploreVideo, String> {}
