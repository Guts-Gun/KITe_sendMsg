package gutsandgun.kite_sendmsg.feignClients;

import gutsandgun.kite_sendmsg.dto.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.dto.ReplaceSendingBodyDTO;
import gutsandgun.kite_sendmsg.feignClients.broker.FeignBrokerConfig;
import gutsandgun.kite_sendmsg.feignClients.sending.FeignSendingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "sendingFeignClient", url = "${feign.url.sending}", configuration = FeignSendingConfig.class)
public interface SendingFeignClient {
	@PostMapping("/sending/replaceSend/Msg")
	ResponseEntity<String> sendSms(@RequestBody ReplaceSendingBodyDTO sendingBodyDTO);
}
