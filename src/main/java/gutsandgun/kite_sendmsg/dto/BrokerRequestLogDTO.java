package gutsandgun.kite_sendmsg.dto;

import gutsandgun.kite_sendmsg.feignClients.error.ErrorCode;
import gutsandgun.kite_sendmsg.type.SendingType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    //객체 용
    public BrokerRequestLogDTO(Long brokerId,SendingDto sendingDto,SendManagerMsgDTO sendManagerMsgDTO){
        this.sendingId = sendingDto.getId();
        this.sendingType = sendingDto.getSendingType();
        this.brokerId = brokerId;
        this.TXId = sendManagerMsgDTO.getId();
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

    //직접 입력 용
    /*
    public BrokerRequestLogDTO(Long sendingId, SendingType sendingType, Long brokerId, Long txId){
        this.sendingId = sendingId;
        this.sendingType = sendingType;
        this.brokerId = brokerId;
        this.TXId = txId;
    }
     */
}
