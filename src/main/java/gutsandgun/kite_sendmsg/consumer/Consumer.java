package gutsandgun.kite_sendmsg.consumer;


import gutsandgun.kite_sendmsg.dto.SendingMsgDTO;
import gutsandgun.kite_sendmsg.service.SendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    @Autowired
    private SendingService sendingService;
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // hello 큐의 메시지가 컨슘되는지 확인하기위해 로그 추가
    @RabbitListener(queues = "${rabbitmq.routing.key.queue1}")
    public void consumeSKT(SendingMsgDTO sendingMsgDTO){
        log.info("SKT message: {}",sendingMsgDTO);

        //redis input 확인용
        //ValueOperations<String, String> vop = redisTemplate.opsForValue();
        //vop.set("header", message.getMessageProperties().getHeader("header"));
        //.getSendingRule(Long.parseLong(message.getMessageProperties().getHeader("id")));
    }

    // hello 큐의 메시지가 컨슘되는지 확인하기위해 로그 추가
    @RabbitListener(queues = "${rabbitmq.routing.key.queue2}")
    public void consumeKT(SendingMsgDTO sendingMsgDTO){
        log.info("KT message: {}",sendingMsgDTO);

        //redis input 확인용
        //ValueOperations<String, String> vop = redisTemplate.opsForValue();
        //vop.set("header", message.getMessageProperties().getHeader("header"));
        //.getSendingRule(Long.parseLong(message.getMessageProperties().getHeader("id")));
    }

    // hello 큐의 메시지가 컨슘되는지 확인하기위해 로그 추가
    @RabbitListener(queues = "${rabbitmq.routing.key.queue3}")
    public void consumeLG(SendingMsgDTO sendingMsgDTO){
        log.info("LG message: {}",sendingMsgDTO);

        //redis input 확인용
        //ValueOperations<String, String> vop = redisTemplate.opsForValue();
        //vop.set("header", message.getMessageProperties().getHeader("header"));
        //.getSendingRule(Long.parseLong(message.getMessageProperties().getHeader("id")));
    }





}
