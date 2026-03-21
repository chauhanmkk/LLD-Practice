package MovieBooking;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BookingController {
    ReentrantLock lock = new ReentrantLock();

     void bookSeat(List<ShowSeat> showSeatList, int userId) {

        try {
            lock.lock();
            for(ShowSeat seats : showSeatList) {
                if (!seats.status.equals("AVAILABLE")) {
                    throw new RuntimeException("Seat not available");
                }
//                seats.status.set("LOCKED");
            }
            //redirect payment
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        finally {
            lock.unlock();
        }
    }
}
