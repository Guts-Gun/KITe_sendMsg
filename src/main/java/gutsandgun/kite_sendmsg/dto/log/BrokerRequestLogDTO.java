package gutsandgun.kite_sendmsg.dto.log;

import gutsandgun.kite_sendmsg.dto.sendMsg.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendManagerMsgDTO;
import gutsandgun.kite_sendmsg.dto.SendingDto;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendMsgProceessingDTO;
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

    String sender;

    String receiver;

    String content;

    Long time = new Date().getTime();

    //객체 용
    public BrokerRequestLogDTO( Long brokerId,SendMsgProceessingDTO sendMsgProceessingDTO){
        this.brokerId = brokerId;

        this.sendingId = sendMsgProceessingDTO.getSendingId();
        this.sendingType = sendMsgProceessingDTO.getSendingType();
        this.TXId = sendMsgProceessingDTO.getTxId();

        this.sender = sendMsgProceessingDTO.getBrokerMsgDTO().getSender();
        this.receiver =sendMsgProceessingDTO.getBrokerMsgDTO().getReceiver();
        this.content = sendMsgProceessingDTO.getBrokerMsgDTO().getContent();
    }

    @Override
    public String toString() {
        return "Service: " + Service +
                ", type: " + type +
                ", sendingId: " + sendingId +
                ", sendingType: " + sendingType +
                ", brokerId: " + brokerId +
                ", TXId: " + TXId +
                ", sender: " + sender +
                ", receiver: " + receiver +
                ", content: " + content +
                ", time: " + time +
                "@";
    }
}
