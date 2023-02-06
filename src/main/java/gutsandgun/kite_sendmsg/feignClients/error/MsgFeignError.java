package gutsandgun.kite_sendmsg.feignClients.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class MsgFeignError implements ErrorDecoder {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Exception decode(String s, Response response) {

        switch (response.status()) {
            case 400:
                log.info("borker response 에러");
                log.info(String.valueOf(response.status()));

                BrokerResponseErrorDto message = null;
                try (InputStream bodyIs = response.body()
                        .asInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    message = mapper.readValue(bodyIs, BrokerResponseErrorDto.class);
                } catch (IOException e) {
                    return new Exception(e.getMessage());
                }
                log.info(message.toString());
                for(ErrorCode c : ErrorCode.values()){
                    if(message.getCode().equals(c.getCode())){
                        log.info("error code : {}" , c.getCode());
                        return new BrokerErrorException(c.getCode());
                    }
                }
        }
        return null;
    }
}
