package gutsandgun.kite_sendmsg.consumer;


import gutsandgun.kite_sendmsg.dto.*;
import gutsandgun.kite_sendmsg.service.SendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    //service
    @Autowired
    private SendingService sendingService;

    @RabbitListener(queues = "${rabbitmq.routing.key.queue1}")
    public void consumeSKT(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("SKT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        //2.sending 정보 얻기
        SendingDto sendingDto = null;
        //!!! 사용자 오류 !!!
        sendingDto = sendingService.getSendingToDto(sendManagerMsgDTO.getSendingId());
        log.info("sendingDto: {}",sendingDto);
        log.info("-----------------------------");

        //3.broker msg만들기 (with msg 처리)
        BrokerMsgDTO brokerMsgDTO = sendingService.makeBrokerMessage(sendingDto,sendManagerMsgDTO);
        log.info("-----------------------------");

        //4.api send
        sendingService.sendBroker(1L,brokerMsgDTO,sendingDto,sendManagerMsgDTO);
        log.info("============================");
    }

    @RabbitListener(queues = "${rabbitmq.routing.key.queue2}")
    public void consumeKT(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("KT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        //2.sending 정보 얻기
        SendingDto sendingDto = null;
        //!!! 사용자 오류 !!!
        sendingDto = sendingService.getSendingToDto(sendManagerMsgDTO.getSendingId());

        log.info("-----------------------------");

        //3.broker msg만들기 (with msg 처리)
        BrokerMsgDTO brokerMsgDTO = sendingService.makeBrokerMessage(sendingDto,sendManagerMsgDTO);
        log.info("-----------------------------");

        //4.api send
        sendingService.sendBroker(2L,brokerMsgDTO,sendingDto,sendManagerMsgDTO);
        log.info("============================");

    }

    @RabbitListener(queues = "${rabbitmq.routing.key.queue3}")
    public void consumeLG(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("LG message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        //2.sending 정보 얻기
        SendingDto sendingDto = null;
        //!!! 사용자 오류 !!!
        sendingDto = sendingService.getSendingToDto(sendManagerMsgDTO.getSendingId());

        log.info("-----------------------------");

        //3.broker msg만들기 (with msg 처리)
        BrokerMsgDTO brokerMsgDTO = sendingService.makeBrokerMessage(sendingDto,sendManagerMsgDTO);
        log.info("-----------------------------");

        //4.api send
        sendingService.sendBroker(3L,brokerMsgDTO,sendingDto,sendManagerMsgDTO);
        log.info("============================");
    }

}
