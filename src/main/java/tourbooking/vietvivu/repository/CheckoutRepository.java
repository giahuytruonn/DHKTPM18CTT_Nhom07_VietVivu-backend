package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Checkout;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, String> {
    // Thống kê theo phương thức thanh toán
    @Query(
            """
		SELECT c.paymentMethod AS paymentMethod, COUNT(c) AS count
		FROM Checkout c
		GROUP BY c.paymentMethod
	""")
    List<Object[]> countByPaymentMethod();
}
