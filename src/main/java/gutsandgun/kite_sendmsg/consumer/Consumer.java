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
        SendingDto sendingDto = sendingService.getSendingToDto(sendManagerMsgDTO.getSendingId());
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
        log.info("문자 자리수: {}",content.length());
        log.info("문자 bytes: {}",content.getBytes().length);
        if(sendingDto.getSendingType()==SendingType.SMS && content.length() >= 40){
            content = content.substring(0,40-1);
        }
        //mms일때 lms bytes 수 > 2000
        if(sendingDto.getSendingType()==SendingType.MMS && content.getBytes().length >= 1000){
            content = content.substring(0,1000-1);
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





}
