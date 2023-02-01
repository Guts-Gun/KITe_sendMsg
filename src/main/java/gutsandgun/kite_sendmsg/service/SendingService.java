package gutsandgun.kite_sendmsg.service;

import gutsandgun.kite_sendmsg.entity.read.Sending;
import gutsandgun.kite_sendmsg.repository.read.ReadSendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SendingService {

    @Autowired
    ReadSendingRepository readSendingRepository;


    @Cacheable(value="test" , key = "#id" )
    public String getSendingRule(Long id){
        //redis get 확인용
        Optional<Sending> value = readSendingRepository.findById(id);

        if(value.isPresent()){
            return value.get().toString();
        }
        else{
            return "";
        }

    }



}
