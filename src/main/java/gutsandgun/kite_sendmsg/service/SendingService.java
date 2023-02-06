package gutsandgun.kite_sendmsg.service;

import gutsandgun.kite_sendmsg.dto.SendingDto;
import gutsandgun.kite_sendmsg.entity.read.Sending;
import gutsandgun.kite_sendmsg.repository.read.ReadSendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SendingService {
    //redis
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    ReadSendingRepository readSendingRepository;

    @Cacheable(value="test" , key = "#id" ,cacheManager = "redisCacheManager")
    public SendingDto getSendingToDto(Long id){
        Sending sending = getSendingEntity(id);
        SendingDto sendingDto = new SendingDto(sending);
        return sendingDto;
    }

        public Sending getSendingEntity(Long id){
            Optional<Sending> value = readSendingRepository.findById(id);
            return value.get();
        }

}
