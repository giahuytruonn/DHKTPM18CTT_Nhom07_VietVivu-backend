package tourbooking.vietvivu.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import feign.Param;
import tourbooking.vietvivu.entity.Invoice;

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


    @Query("SELECT i.booking.tour.title, SUM(i.amount) "
            + "FROM Invoice i "
            + "WHERE i.dateIssued BETWEEN :startTime AND :endTime "
            + "GROUP BY i.booking.tour.title "
            + "ORDER BY SUM(i.amount) DESC")
    List<Object[]> getRevenueByTour(@Param("startTime") LocalDate startTime,
                                    @Param("endTime") LocalDate endTime);
}
