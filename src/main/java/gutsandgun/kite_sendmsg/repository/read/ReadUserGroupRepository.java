package gutsandgun.kite_sendmsg.repository.read;

import gutsandgun.kite_sendmsg.entity.read.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadUserGroupRepository extends JpaRepository<UserGroup, Long> {
}
