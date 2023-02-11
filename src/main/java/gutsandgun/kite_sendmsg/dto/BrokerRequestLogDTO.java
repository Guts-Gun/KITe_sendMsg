package gutsandgun.kite_sendmsg.dto;

import gutsandgun.kite_sendmsg.type.SendingType;
import lombok.Data;

import java.util.Date;

@Data
public class BrokerRequestLogDTO {
    String Service = "Send";
    String type = "sendBroker";

    Long sendingId;
    SendingType sendingType;
    Long brokerId;
    Long TXId;

    Long time = new Date().getTime();

    String content;

    //객체 용
    public BrokerRequestLogDTO(Long brokerId,SendingDto sendingDto,BrokerMsgDTO brokerMsgDTO,SendManagerMsgDTO sendManagerMsgDTO){
        this.sendingId = sendingDto.getId();
        this.sendingType = sendingDto.getSendingType();
        this.brokerId = brokerId;
        this.TXId = sendManagerMsgDTO.getId();
        this.content = brokerMsgDTO.getContent();
    }

    @Override
    public String toString() {
        return "Service=" + Service +
                ", type=" + type +
                ", sendingId=" + sendingId +
                ", sendingType=" + sendingType +
                ", brokerId=" + brokerId +
                ", TXId=" + TXId +
                ", time=" + time;
    }
}
