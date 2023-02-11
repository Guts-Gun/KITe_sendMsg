package gutsandgun.kite_sendmsg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendmsg.dto.*;
import gutsandgun.kite_sendmsg.entity.read.Broker;
import gutsandgun.kite_sendmsg.entity.read.Sending;
import gutsandgun.kite_sendmsg.feignClients.SendingFeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsFeignClient;
import gutsandgun.kite_sendmsg.feignClients.broker.error.BrokerErrorException;
import gutsandgun.kite_sendmsg.feignClients.broker.error.ErrorCode;
import gutsandgun.kite_sendmsg.repository.read.ReadBrokerRepository;
import gutsandgun.kite_sendmsg.repository.read.ReadSendingRepository;
import gutsandgun.kite_sendmsg.type.FailReason;
import gutsandgun.kite_sendmsg.type.SendingStatus;
import gutsandgun.kite_sendmsg.type.SendingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SendingService {
    private static final Logger log = LoggerFactory.getLogger(SendingService.class);
    //read db
    @Autowired
    ReadSendingRepository readSendingRepository;

    @Autowired
    ReadBrokerRepository readBrokerRepository;


    //api
    @Autowired
    private SmsFeignClient smsFeignClient;
    @Autowired
    private SendingFeignClient sendingFeignClient;

    public BrokerMsgDTO makeBrokerMessage(SendingDto sendingDto, SendManagerMsgDTO sendManagerMsgDTO){
        //3.broker msg만들기
        //3-1 문자열 치환
        //지금은 이름만 message의 %고객명% 부분에 name넣기
        String content = sendingDto.getContent();
        log.info("Message: {}",content);
        content = content.replace("%고객명%", sendManagerMsgDTO.getName());
        log.info("Message parsing: {}",content);
        log.info("-----------------------------");

        //3-2.초과 처리
        //sms일때 sms bytes 수 > 80
        if(sendingDto.getSendingType() == SendingType.SMS) {
            content = sliceMsg(content,160);
        }
        //mms일때 lms bytes 수 > 2000
        if(sendingDto.getSendingType()==SendingType.MMS){
            content = sliceMsg(content,2000);
        }
        log.info("Message exceed: {}",content);

        //3-3. broker msg 만들기
        BrokerMsgDTO brokerMsgDTO = new BrokerMsgDTO(sendingDto,sendManagerMsgDTO);
        brokerMsgDTO.setContent(content);

        return brokerMsgDTO;
    }
        String sliceMsg(String msg,int byteLength){
            int bytes = 0;
            if (msg == null) {
                return msg;
            } else {
                char[] strChar = msg.toCharArray();

                int charLength = 0;
                for (int i = 0; i < strChar.length; i++) {
                    int code = strChar[i];

                    // 2bytes
                    if (code >= 128) bytes += 2;
                        // 1bytes
                    else bytes +=1;

                    if(bytes < byteLength){
                        charLength += 1;
                    }
                    else{
                        log.info("문자 자리수: {}",charLength);
                        log.info("문자 bytes: {}",bytes);
                        return msg.substring(0,charLength);
                    }
                }
                log.info("문자 자리수: {}",charLength);
                log.info("문자 bytes: {}",bytes);
                return msg;
            }
        }


    //4. api send
    public void sendBroker(Long brokerId,BrokerMsgDTO brokerMsgDTO,SendingDto sendingDto, SendManagerMsgDTO sendManagerMsgDTO){
        boolean firstBrokerSendSuccess = true;
        BrokerResponseLogDTO firstBrokerResponseLogDTO = new BrokerResponseLogDTO(brokerId, SendingStatus.COMPLETE, sendingDto,sendManagerMsgDTO);
        try {
            log.info("Send brokder: {}",brokerMsgDTO);
            BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(brokerId,sendingDto,sendManagerMsgDTO);
            log.info("log: "+ brokerRequestLogDTO.toString());
            log.info("-----------------------------");
            ResponseEntity<Long> response = smsFeignClient.sendSms(brokerMsgDTO);
        }
        catch (BrokerErrorException e){
            System.out.println(e);
            firstBrokerSendSuccess = false;
            log.info("*******************************************");
            System.out.println("Error:" + e.getMessage());
            firstBrokerResponseLogDTO.setTime();
            firstBrokerResponseLogDTO.setSuccess(SendingStatus.FAIL);
            if(e.getMessage().equals(ErrorCode.BAD_REQUEST.getCode())){
                //1.중계사 대체 발송
                firstBrokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                log.info("log: "+firstBrokerResponseLogDTO.toString());

                List<BrokerDTO> brokerDTOList = getMsgBrokerList();
                ArrayList<Boolean> brokerResponseList = new ArrayList<Boolean>();
                log.info("brokerList:{}",brokerDTOList);
                log.info("-----------------------------");
                //대체 발송 처리(sending queue)
                for (BrokerDTO b : brokerDTOList){
                    if(brokerId == b.getId()){
                        brokerResponseList.add(false);
                    }
                    else{
                        Boolean alternativeBrokerSuccess = true;
                        try{
                            log.info("Replace send broker: {}", b.getId());
                            BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(),sendingDto,sendManagerMsgDTO);
                            log.info("log: "+ brokerRequestLogDTO.toString());
                            log.info("-----------------------------");
                            //client어케 보내지 ?? 음;;
                            ResponseEntity<Long> response = smsFeignClient.sendSms(brokerMsgDTO);
                        }
                        catch (BrokerErrorException ee){
                            System.out.println(ee);
                            System.out.println("Error:" + ee.getMessage());
                            if(e.getMessage().equals(ErrorCode.BAD_REQUEST.getCode())){
                                alternativeBrokerSuccess = false;
                                BrokerResponseLogDTO brokerResponseLogDTOForAlternative = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL, sendingDto,sendManagerMsgDTO);
                                brokerResponseLogDTOForAlternative.setFailReason(FailReason.BAD_REQUEST);
                                log.info("log: "+brokerResponseLogDTOForAlternative.toString());
                                log.info("-----------------------------");
                            }
                        }
                        finally {
                            if(alternativeBrokerSuccess){
                                brokerResponseList.add(true);
                                BrokerResponseLogDTO alternativeBrokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE, sendingDto,sendManagerMsgDTO);
                                log.info("log: "+alternativeBrokerResponseLogDTO.toString());
                                log.info("-----------------------------");
                                break;
                            }
                            else{
                                brokerResponseList.add(false);
                            }
                        }
                    }
                }
            }
            else if(e.getMessage().equals(ErrorCode.INVALID_PHONE.getCode())){
                //2.수신거부/전화번호 없음 로그 기록
                firstBrokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                log.info("log: "+firstBrokerResponseLogDTO.toString());
                //이메일 대체 발송api전송
                log.info("replace Check : {}", sendingDto.getReplaceYn());
                if(sendingDto.getReplaceYn().equals("Y")){
                    ReplaceSendingBodyDTO replaceSendingBodyDTO = new ReplaceSendingBodyDTO(3L,1L);
                    log.info("이메일 대체 발송: {}",replaceSendingBodyDTO);
                    ResponseEntity<String> response = sendingFeignClient.sendSms(replaceSendingBodyDTO);
                    log.info("이메일 대체 발송 결과: {}",response.getStatusCode());
                }
            }
            //other오류도 처리해야하는지?
            log.info("*******************************************");
        }
        finally {
            if(firstBrokerSendSuccess){
                firstBrokerResponseLogDTO.setTime();
                log.info("log: "+firstBrokerResponseLogDTO.toString());
                log.info("-----------------------------");
            }
        }
    }




    @Cacheable(value="test" , key = "#id" ,cacheManager = "redisCacheManager")
    public SendingDto getSendingToDto(Long id){
        //with log
        long beforeTime = System.currentTimeMillis();

        Sending sending = getSendingEntity(id);
        SendingDto sendingDto = new SendingDto(sending);

        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime);
        log.info("처리 속도(using cache) : "+secDiffTime);
        log.info("sending 정보: {}",sendingDto);

        return sendingDto;
    }

        public Sending getSendingEntity(Long id){
            Optional<Sending> value = readSendingRepository.findById(id);
            return value.get();
        }


    public List<BrokerDTO> getMsgBrokerList(){
        List<Broker> BrokerList = readBrokerRepository.findBySendingType(SendingType.SMS);
        List<BrokerDTO> brokerDTOList = new ArrayList<>();
        BrokerList.forEach(broker -> {
            brokerDTOList.add(mapper.convertValue(broker,BrokerDTO.class));
        });

        return brokerDTOList;
    }

}
