package tourbooking.vietvivu.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Invoice;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    @Query("SELECT SUM(i.amount) FROM Invoice i")
    Double getTotalRevenue();

    @Query("SELECT MONTH(i.dateIssued), SUM(i.amount) "
            + "FROM Invoice i "
            + "WHERE YEAR(i.dateIssued) =:year "
            + "GROUP BY MONTH(i.dateIssued) "
            + "ORDER BY MONTH(i.dateIssued)")
    List<Object[]> getAmountGroupedByMonth(@Param("year") int year);


}
