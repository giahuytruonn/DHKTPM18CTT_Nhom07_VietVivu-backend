package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, String> {}
