package gutsandgun.kite_sendmsg.dto.log;

import gutsandgun.kite_sendmsg.dto.sendMsg.SendManagerMsgDTO;
import gutsandgun.kite_sendmsg.dto.SendingDto;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendMsgProceessingDTO;
import gutsandgun.kite_sendmsg.type.FailReason;
import gutsandgun.kite_sendmsg.type.SendingStatus;
import gutsandgun.kite_sendmsg.type.SendingType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
public class BrokerResponseLogDTO {
    String Service = "Send";
    String type = "receiveBroker";

    boolean success;
    FailReason failReason;

    Long sendingId;
    SendingType sendingType;
    Long brokerId;
    Long TXId;

    Long time = new Date().getTime();

    //객체 용
    public BrokerResponseLogDTO( Long brokerId, boolean success, SendMsgProceessingDTO sendMsgProceessingDTO){
        this.success = success;
        this.brokerId = brokerId;

        this.sendingId = sendMsgProceessingDTO.getSendingId();
        this.sendingType = sendMsgProceessingDTO.getSendingType();
        this.TXId = sendMsgProceessingDTO.getTxId();
    }

    public void setFailReason(FailReason failReason) {
        this.failReason = failReason;
    }

    @Override
    public String toString() {
        return  "Service: " + Service +
                ", type: " + type +
                ", success: " + success +
                ", failReason: " + failReason +
                ", sendingId: " + sendingId +
                ", sendingType: " + sendingType +
                ", brokerId: " + brokerId +
                ", TXId: " + TXId +
                ", time: " + time +
                "@";
    }


}



