package gutsandgun.kite_sendmsg.feignClients.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BrokerResponseErrorDto {
    String code;
    String message;
}
