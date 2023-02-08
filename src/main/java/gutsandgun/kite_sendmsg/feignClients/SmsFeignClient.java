package gutsandgun.kite_sendmsg.feignClients;

import gutsandgun.kite_sendmsg.dto.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.feignClients.broker.FeignBrokerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "smsFeignClient", url = "${feign.url.broker-dummy}", configuration = FeignBrokerConfig.class)
public interface SmsFeignClient {
	@PostMapping("/broker/send/sms")
	ResponseEntity<Long> sendSms(@RequestBody BrokerMsgDTO brokerMsgDTO);

}
