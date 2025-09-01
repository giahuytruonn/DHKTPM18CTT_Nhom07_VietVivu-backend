package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Checkout;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, String> {
}
