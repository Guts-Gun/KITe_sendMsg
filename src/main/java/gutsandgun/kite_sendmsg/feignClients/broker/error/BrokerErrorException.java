package gutsandgun.kite_sendmsg.feignClients.broker.error;

public class BrokerErrorException extends RuntimeException{
    public BrokerErrorException(String errorCode) {
        super(errorCode);
    }
}
