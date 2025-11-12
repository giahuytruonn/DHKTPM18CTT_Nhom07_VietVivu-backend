package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {}
