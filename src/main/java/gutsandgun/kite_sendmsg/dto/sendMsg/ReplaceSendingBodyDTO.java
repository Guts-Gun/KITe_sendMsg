package gutsandgun.kite_sendmsg.dto.sendMsg;

import lombok.*;

@Getter
@Setter
@ToString
@Data
@AllArgsConstructor
public class ReplaceSendingBodyDTO {
    Long sendingId;
    Long txId;
}
