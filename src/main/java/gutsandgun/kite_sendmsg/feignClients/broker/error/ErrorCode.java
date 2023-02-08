package gutsandgun.kite_sendmsg.feignClients.broker.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    BAD_REQUEST(400, "S000", "잘못된 요청입니다."),
    INVALID_PHONE(400, "ERR404", "잘못된 전화번호입니다.");



    private final int status;
    private final String code;
    private final String message;

}
