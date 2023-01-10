package gutsandgun.kite_sendmsg.repository.read;

import gutsandgun.kite_sendmsg.entity.read.SendingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadSendingScheduleRepository extends JpaRepository<SendingSchedule, Long> {
}
