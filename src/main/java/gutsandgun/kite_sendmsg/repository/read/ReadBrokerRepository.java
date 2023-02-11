package gutsandgun.kite_sendmsg.repository.read;

import gutsandgun.kite_sendmsg.entity.read.Broker;
import gutsandgun.kite_sendmsg.type.SendingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadBrokerRepository extends JpaRepository<Broker, Long> {

    List<Broker> findBySendingType(SendingType sendingType);

}
