package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {}
