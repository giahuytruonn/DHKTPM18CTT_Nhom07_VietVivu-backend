package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
