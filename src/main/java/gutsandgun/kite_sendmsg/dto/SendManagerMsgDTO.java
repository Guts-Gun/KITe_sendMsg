package gutsandgun.kite_sendmsg.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SendManagerMsgDTO {
    private Long id;
    private Long sendingId;
    private String sender;
    private String receiver;
    private String name;
    private String regId;
    private String modId;
}