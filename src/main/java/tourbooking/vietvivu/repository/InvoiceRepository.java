package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
}
