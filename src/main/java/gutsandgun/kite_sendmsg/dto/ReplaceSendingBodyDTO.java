package gutsandgun.kite_sendmsg.dto;

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
