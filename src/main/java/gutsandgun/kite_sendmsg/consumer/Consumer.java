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

    // SKT
    @RabbitListener(queues = "${rabbitmq.routing.key.queue1}")
    public void consumeSKT(SendManagerMsgDTO sendManagerMsgDTO){
        Long brokerId = 1L;
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
        sendingService.sendBroker(brokerId,brokerMsgDTO,sendingDto,sendManagerMsgDTO);
        log.info("============================");
    }

    // KT
    @RabbitListener(queues = "${rabbitmq.routing.key.queue2}")
    public void consumeKT(SendManagerMsgDTO sendManagerMsgDTO){
        Long brokerId = 2L;
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
        sendingService.sendBroker(brokerId,brokerMsgDTO,sendingDto,sendManagerMsgDTO);
        log.info("============================");

    }

    // LG
    @RabbitListener(queues = "${rabbitmq.routing.key.queue3}")
    public void consumeLG(SendManagerMsgDTO sendManagerMsgDTO){
        Long brokerId = 3L;
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
        sendingService.sendBroker(brokerId,brokerMsgDTO,sendingDto,sendManagerMsgDTO);
        log.info("============================");
    }

}
