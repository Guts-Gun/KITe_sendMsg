package gutsandgun.kite_sendmsg.feignClients;

import gutsandgun.kite_sendmsg.dto.sendMsg.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.feignClients.broker.FeignBrokerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "smsFeignClient", url = "${feign.url.broker-dummy2}",contextId = "broker2" , configuration = FeignBrokerConfig.class)
public interface SmsBroker2FeignClient {
	@PostMapping("/broker/all/{brokerName}/send/sms")
	ResponseEntity<Long> sendSms(@PathVariable("brokerName") String name,@RequestBody BrokerMsgDTO brokerMsgDTO);

}
