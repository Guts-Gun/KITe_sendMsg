package gutsandgun.kite_sendmsg.feignClients.error;

public class BrokerErrorException extends RuntimeException{
    public BrokerErrorException(String errorCode) {
        super(errorCode);
    }
}
