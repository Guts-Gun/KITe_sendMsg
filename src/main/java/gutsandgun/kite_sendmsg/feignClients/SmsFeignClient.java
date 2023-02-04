package gutsandgun.kite_sendmsg.feignClients;

import gutsandgun.kite_sendmsg.dto.BrokerMsgDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "smsFeignClient", url = "${feign.url.broker-dummy}", configuration = FeignConfig.class)
public interface SmsFeignClient {
	@PostMapping("/broker/send/sms")
	ResponseEntity<Long> sendSms(@RequestBody BrokerMsgDTO brokerMsgDTO);

}
