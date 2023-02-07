package gutsandgun.kite_sendmsg.consumer;


import gutsandgun.kite_sendmsg.dto.*;
import gutsandgun.kite_sendmsg.feignClients.SmsFeignClient;
import gutsandgun.kite_sendmsg.feignClients.error.BrokerErrorException;
import gutsandgun.kite_sendmsg.feignClients.error.ErrorCode;
import gutsandgun.kite_sendmsg.service.SendingService;
import gutsandgun.kite_sendmsg.type.FailReason;
import gutsandgun.kite_sendmsg.type.SendingStatus;
import gutsandgun.kite_sendmsg.type.SendingType;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private SendingService sendingService;

    //api
    @Autowired
    private SmsFeignClient smsFeignClient;



    @RabbitListener(queues = "${rabbitmq.routing.key.queue1}")
    public void consumeSKT(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("SKT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        //2.broker msg(새로 만들기)
        //2-1.sending 정보 얻기
        System.out.println(sendManagerMsgDTO.getSendingId());
        long beforeTime = System.currentTimeMillis();
        SendingDto sendingDto = sendingService.getSendingToDto(sendManagerMsgDTO.getSendingId());
        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime);
        log.info("처리 속도(using cache) : "+secDiffTime);
        log.info("sending 정보: {}",sendingDto);
        log.info("-----------------------------");
        //2-2.replace==yes일때 message합치기
            //지금은 이름만 message의 %고객명% 부분에 name넣기
        String content = sendingDto.getContent();
        log.info("Message: {}",content);
        content = content.replace("%고객명%", sendManagerMsgDTO.getName());
        log.info("Message parsing: {}",content);
        log.info("-----------------------------");

        //2-3.초과 처리
        //sms일때 sms bytes 수 > 80
        if(sendingDto.getSendingType()==SendingType.SMS) {
            content = sliceMsg(content,160);
        }
        //mms일때 lms bytes 수 > 2000
        if(sendingDto.getSendingType()==SendingType.MMS){
            content = sliceMsg(content,2000);
        }
        log.info("Message exceed: {}",content);
        log.info("-----------------------------");

        //2-3.brokerMsgDto 생성
        BrokerMsgDTO brokerMsgDTO = new BrokerMsgDTO(sendingDto,sendManagerMsgDTO);
        brokerMsgDTO.setContent(content);

        //3.api send
        boolean brokerSendSuccess=true;
        BrokerRequestLogDTO brokerRequestLogDTO;
        BrokerResponseLogDTO brokerResponseLogDTO;
        try {
            log.info("Send brokder: {}",brokerMsgDTO);
            brokerRequestLogDTO = new BrokerRequestLogDTO(1L,sendingDto,sendManagerMsgDTO);
            log.info("log: "+ brokerRequestLogDTO.toString());
            log.info("-----------------------------");
            ResponseEntity<Long> response = smsFeignClient.sendSms(brokerMsgDTO);
        }
        catch (BrokerErrorException e){
            System.out.println(e);
            brokerSendSuccess = false;
            log.info("Response broker isSuccess:{}",brokerSendSuccess);
            System.out.println("Error:" + e.getMessage());
            brokerResponseLogDTO = new BrokerResponseLogDTO(1L,SendingStatus.FAIL, sendingDto,sendManagerMsgDTO);
            if(e.getMessage().equals(ErrorCode.BAD_REQUEST.getCode())){
                //1.중계사 대체 발송
                brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                log.info("log: "+brokerResponseLogDTO.toString());
                //!!대체 발송 처리!
            }
            else if(e.getMessage().equals(ErrorCode.INVALID_PHONE.getCode())){
                //2.수신거부/전화번호 없음 로그 기록
                brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                log.info("log: "+brokerResponseLogDTO.toString());
            }
            //other오류도 처리해야하는지?
        }
        log.info("Response broker isSuccess:{}",brokerSendSuccess);
        System.out.println("Success00000");
        if(brokerSendSuccess==true){
            brokerResponseLogDTO = new BrokerResponseLogDTO(1L,SendingStatus.COMPLETE, sendingDto,sendManagerMsgDTO);
            log.info("log: "+brokerResponseLogDTO.toString());
        }
        log.info("============================");
    }

    @RabbitListener(queues = "${rabbitmq.routing.key.queue2}")
    public void consumeKT(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("KT message: {}",sendManagerMsgDTO);
    }

    @RabbitListener(queues = "${rabbitmq.routing.key.queue3}")
    public void consumeLG(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("LG message: {}",sendManagerMsgDTO);
    }

    String sliceMsg(String msg,int byteLength){
        int bytes = 0;
        if (msg == null) {
            return msg;
        } else {
            char[] strChar = msg.toCharArray();

            int charLength = 0;
            for (int i = 0; i < strChar.length; i++) {
                int code = strChar[i];

                // 2bytes
                if (code >= 128) bytes += 2;
                    // 1bytes
                else bytes +=1;

                if(bytes < byteLength){
                    charLength += 1;
                }
                else{
                    log.info("문자 자리수: {}",charLength);
                    log.info("문자 bytes: {}",bytes);
                   return msg.substring(0,charLength);
                }
            }
            log.info("문자 자리수: {}",charLength);
            log.info("문자 bytes: {}",bytes);
            return msg;
        }
    }

}
