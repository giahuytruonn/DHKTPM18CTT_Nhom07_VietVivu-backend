package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.BookingRequest;
import tourbooking.vietvivu.enumm.ActionType;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, String> {

    List<BookingRequest> findBookingRequestsByRequestType(ActionType requestType);
}
