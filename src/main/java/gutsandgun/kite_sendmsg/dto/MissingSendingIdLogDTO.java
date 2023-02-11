package gutsandgun.kite_sendmsg.dto;

import gutsandgun.kite_sendmsg.type.FailReason;
import gutsandgun.kite_sendmsg.type.SendingStatus;
import gutsandgun.kite_sendmsg.type.SendingType;
import lombok.Data;

import java.util.Date;

@Data
public class MissingSendingIdLogDTO {
    String Service = "Send";
    String type = "missingSendingId";

    Long sendingId;
    //FailReason failReason;
    Long brokerId;
    Long TXId;
    Long time = new Date().getTime();

    public MissingSendingIdLogDTO(Long brokerId,SendManagerMsgDTO sendManagerMsgDTO){
        this.sendingId = sendManagerMsgDTO.getSendingId();
        this.brokerId = brokerId;
        this.TXId = sendManagerMsgDTO.getId();
    }

    @Override
    public String toString() {
        return  "Service='" + Service +
                ", type='" + type +
                ", sendingId=" + sendingId +
                ", brokerId=" + brokerId +
                ", TXId=" + TXId +
                ", time=" + time;
    }
}
