package gutsandgun.kite_sendmsg.consumer;


import gutsandgun.kite_sendmsg.dto.*;
import gutsandgun.kite_sendmsg.dto.log.MissingSendingIdLogDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendManagerMsgDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendMsgProceessingDTO;
import gutsandgun.kite_sendmsg.exception.ConsumerException;
import gutsandgun.kite_sendmsg.service.SendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    //service
    @Autowired
    private SendingService sendingService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1000);

    // SKT
    @RabbitListener(queues = "${rabbitmq.routing.key.queue1}", concurrency = "10")
    public void consumeSKT(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("Consume | txId : {} Time : {}",sendManagerMsgDTO.getId(),new Date().getTime());
        Long brokerId = 1L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1. SKT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        SendMsgProceessingDTO sendMsgProceessingDTO = new SendMsgProceessingDTO(brokerId,sendManagerMsgDTO);
        executorService.submit(() ->sendingService.sendMsgProcessing(brokerId,sendMsgProceessingDTO));

        log.info("============================");

    }

    // KT
    @RabbitListener(queues = "${rabbitmq.routing.key.queue2}", concurrency = "10")
    public void consumeKT(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("Consume | txId : {} Time : {}",sendManagerMsgDTO.getId(),new Date().getTime());
        Long brokerId = 2L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1/ KT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        SendMsgProceessingDTO sendMsgProceessingDTO = new SendMsgProceessingDTO(brokerId,sendManagerMsgDTO);
        executorService.submit(() ->sendingService.sendMsgProcessing(brokerId,sendMsgProceessingDTO));

        log.info("============================");

    }

    // LG
    @RabbitListener(queues = "${rabbitmq.routing.key.queue3}", concurrency = "10")
    public void consumeLG(SendManagerMsgDTO sendManagerMsgDTO){
        log.info("Consume | txId : {} Time : {}",sendManagerMsgDTO.getId(),new Date().getTime());
        Long brokerId = 3L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1. LG message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        SendMsgProceessingDTO sendMsgProceessingDTO = new SendMsgProceessingDTO(brokerId,sendManagerMsgDTO);
        executorService.submit(() ->sendingService.sendMsgProcessing(brokerId,sendMsgProceessingDTO));

        log.info("============================");
    }

}
