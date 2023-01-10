package gutsandgun.kite_sendmsg.repository.read;

import gutsandgun.kite_sendmsg.entity.read.UserEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserEmailRepository extends JpaRepository<UserEmail, Long> {
}
