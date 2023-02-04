package gutsandgun.kite_sendmsg.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BrokerMsgDTO {
    private String sender;
    private String receiver;
    private String title;
    private String content;
    private String mediaLink;

    public BrokerMsgDTO(SendingDto sendingDto, SendManagerMsgDTO sendManagerMsgDTO){
        this.sender = sendManagerMsgDTO.getSender();
        this.receiver = sendManagerMsgDTO.getReceiver();
        this.title = sendingDto.getTitle();
        //CONTENT는 문자열 치환후!
        this.mediaLink = sendingDto.getMediaLink();
    }
}
