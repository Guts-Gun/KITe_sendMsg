package gutsandgun.kite_sendmsg.dto;

import gutsandgun.kite_sendmsg.consumer.Consumer;
import gutsandgun.kite_sendmsg.feignClients.error.ErrorCode;
import gutsandgun.kite_sendmsg.type.FailReason;
import gutsandgun.kite_sendmsg.type.SendingStatus;
import gutsandgun.kite_sendmsg.type.SendingType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Data
public class BrokerResponseLogDTO {
    String Service = "Send";
    String type = "receiveBroker";

    SendingStatus success;
    FailReason failReason;

    Long sendingId;
    SendingType sendingType;
    Long brokerId;
    Long TXId;

    Long time = new Date().getTime();

    //객체 용
    public BrokerResponseLogDTO(Long brokerId,SendingStatus success,SendingDto sendingDto, SendManagerMsgDTO sendManagerMsgDTO){
        this.success = success;
        this.sendingId = sendingDto.getId();
        this.sendingType = sendingDto.getSendingType();
        this.brokerId = brokerId;
        this.TXId = sendManagerMsgDTO.getId();
    }

    public void setFailReason(FailReason failReason) {
        this.failReason = failReason;
    }

    @Override
    public String toString() {
        return  "Service=" + Service +
                ", type=" + type +
                ", success=" + success +
                ", failReason=" + failReason +
                ", sendingId=" + sendingId +
                ", sendingType=" + sendingType +
                ", brokerId=" + brokerId +
                ", TXId=" + TXId +
                ", time=" + time;
    }

    //직접입력용
    /*
    public BrokerResponseLogDTO(SendingStatus success, FailReason failReason,Long sendingId, SendingType sendingType,Long  brokerId,Long txId){
        this.success = success;
        this.failReason = failReason;
        this.sendingId = sendingId;
        this.sendingType = sendingType;
        this.brokerId = brokerId;
        this.TXId = txId;
    }
     */

}



