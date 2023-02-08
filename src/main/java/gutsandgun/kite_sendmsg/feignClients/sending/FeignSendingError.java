package gutsandgun.kite_sendmsg.feignClients.sending;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import gutsandgun.kite_sendmsg.feignClients.broker.error.BrokerErrorException;
import gutsandgun.kite_sendmsg.feignClients.broker.error.BrokerResponseErrorDto;
import gutsandgun.kite_sendmsg.feignClients.broker.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

    public class FeignSendingError implements ErrorDecoder {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Exception decode(String s, Response response) {
        switch (response.status()) {
            case 400:
                log.info("email 대체 발송  에러");
                log.info(String.valueOf(response.status()));
        }
        return null;
    }
}
