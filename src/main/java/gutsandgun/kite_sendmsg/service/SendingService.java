package gutsandgun.kite_sendmsg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendmsg.dto.*;
import gutsandgun.kite_sendmsg.dto.log.BrokerRequestLogDTO;
import gutsandgun.kite_sendmsg.dto.log.BrokerResponseLogDTO;
import gutsandgun.kite_sendmsg.dto.log.MissingSendingIdLogDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.ReplaceSendingBodyDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendMsgProceessingDTO;
import gutsandgun.kite_sendmsg.entity.read.Broker;
import gutsandgun.kite_sendmsg.entity.read.Sending;
import gutsandgun.kite_sendmsg.exception.ConsumerException;
import gutsandgun.kite_sendmsg.exception.CustomException;
import gutsandgun.kite_sendmsg.exception.ErrorCode;
import gutsandgun.kite_sendmsg.feignClients.SendingFeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsBroker1FeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsBroker2FeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsBroker3FeignClient;
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

import java.util.*;

@Service
public class SendingService {
    //borker mapping
    Map<Long, String> msgBroker = new HashMap<>() {{
        put(1L, "SKT");
        put(2L, "KT");
        put(3L, "LG");
    }};
    @Autowired
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(SendingService.class);
    //read db

    //repo
    @Autowired
    ReadSendingRepository readSendingRepository;

    @Autowired
    ReadBrokerRepository readBrokerRepository;

    //api
    @Autowired
    private SmsBroker1FeignClient smsBroker1FeignClient;
    @Autowired
    private SmsBroker2FeignClient smsBroker2FeignClient;
    @Autowired
    private SmsBroker3FeignClient smsBroker3FeignClient;
    @Autowired
    private SendingFeignClient sendingFeignClient;

    public void sendMsgProcessing(Long brokerId,SendMsgProceessingDTO sendMsgProceessingDTO){
        try{
            //2.sending 정보 얻기
            sendMsgProceessingDTO.setSendingDto(getSendingDto(sendMsgProceessingDTO.getSendingId()));
            log.info("-----------------------------");
            //3.broker msg만들기 (with msg 처리)
            sendMsgProceessingDTO.setBrokerMsgDTO();
            log.info("-----------------------------");
            //4.발송
            BrokerResponseLogDTO brokerResponseLogDTO = sendBroker(sendMsgProceessingDTO);
            //5.대체발송 (브로커/이메일)
            if(brokerResponseLogDTO.getSuccess().equals(SendingStatus.FAIL)){
                switch (brokerResponseLogDTO.getFailReason()){
                    case BAD_REQUEST :
                        alternativeSendBroker(sendMsgProceessingDTO);
                        break;
                    case INVALID_PHONE:
                        log.info(sendMsgProceessingDTO.getSendingDto().getReplaceYn());
                        if(sendMsgProceessingDTO.getSendingDto().getReplaceYn().equals("Y")){
                            log.info("플랫폼 대체 발송 허가");
                            alternativeSendEmail(sendMsgProceessingDTO);
                        }
                        if(sendMsgProceessingDTO.getSendingDto().getReplaceYn().equals("N")){
                            log.info("플랫폼 대체 발송 X");
                        }
                        break;
                }
            }
        }
        catch(ConsumerException e){
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending 정보 DB 에 없음");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendMsgProceessingDTO);
                System.out.println("log: " + missingSendingIdLogDTO.toString());
            }
            log.info("*******************************************");
        }
    }

        private SendingDto getSendingDto(Long sendingId){
            Sending sending = getSending(sendingId);
            SendingDto sendingDto = new SendingDto(sending);
            return sendingDto;
        }

            @Cacheable(value="sending" , key = "#sendingId" ,cacheManager = "CacheManager")
            public Sending getSending(Long sendingId){
                //with log

                long beforeTime = System.currentTimeMillis();

                Sending sending = readSendingRepository.findById(sendingId).orElseThrow(()-> new ConsumerException(ConsumerException.ERROR_DB));
                long afterTime = System.currentTimeMillis();
                long secDiffTime = (afterTime - beforeTime);
                log.info("2. getSending :{} :",sending.toString());
                log.info("처리 속도(using cache) : "+secDiffTime);

                return sending;
            }






    public BrokerResponseLogDTO sendBroker(SendMsgProceessingDTO sendMsgProceessingDTO){
            BrokerResponseLogDTO brokerResponseLogDTO = null;
            try {
                log.info("4. Send broker: {}",sendMsgProceessingDTO.getBrokerMsgDTO());
                BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(sendMsgProceessingDTO.getBrokerId(),sendMsgProceessingDTO);
                System.out.println("broker[초기발송] request log: "+ brokerRequestLogDTO.toString());
                ResponseEntity<Long> response = sendBrokerApi(sendMsgProceessingDTO.getBrokerId(),sendMsgProceessingDTO.getBrokerMsgDTO());
            }
            catch (CustomException e){
                log.info("*******************************************");
                System.out.println("ERROR : BROKER - " + e.getErrorCode());
                brokerResponseLogDTO = new BrokerResponseLogDTO(sendMsgProceessingDTO.getBrokerId(), SendingStatus.FAIL,sendMsgProceessingDTO);
                if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                    //1. 브로커 오류
                    brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                }
                else if(e.getErrorCode() == ErrorCode.INVALID_PHONE){
                    //2.수신거부/전화번호 없음 로그 기록
                    brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                }
                else{
                    //other오류도 처리해야하는지?
                }
                System.out.println("broker[초기발송] response log: "+ brokerResponseLogDTO.toString());
                log.info("*******************************************");
            }
            finally {
                if(brokerResponseLogDTO==null){
                    brokerResponseLogDTO = new BrokerResponseLogDTO(sendMsgProceessingDTO.getBrokerId(), SendingStatus.COMPLETE,sendMsgProceessingDTO);
                    System.out.println("broker[초기발송] response log: "+brokerResponseLogDTO.toString());
                }
            }
            log.info("-----------------------------");
            return brokerResponseLogDTO;
        }

        public void alternativeSendEmail(SendMsgProceessingDTO sendMsgProceessingDTO){
            ReplaceSendingBodyDTO replaceSendingBodyDTO = new ReplaceSendingBodyDTO(sendMsgProceessingDTO.getSendingId(),sendMsgProceessingDTO.getTxId());
            log.info("5-1. 이메일 대체발송");
            log.info("api[대체발송] request: {}",replaceSendingBodyDTO);
            ResponseEntity<String> response = sendingFeignClient.sendSms(replaceSendingBodyDTO);
            log.info("api[대체발송] response: {}",response.getStatusCode());
        }
        public void alternativeSendBroker(SendMsgProceessingDTO sendMsgProceessingDTO){
            log.info("5-2. 중계사 대체발송");

            //broker 정보 가져오기
            List<BrokerDTO> brokerDTOList = getMsgBrokerList();
            ArrayList<Boolean> brokerResponseList = new ArrayList<Boolean>();
            //log.info("brokerList:{}",brokerDTOList);
            //log.info("-----------------------------");

            //대체 발송 처리(sending queue)
            for (BrokerDTO b : brokerDTOList){
                //최초발송 false처리
                if(sendMsgProceessingDTO.getBrokerId() == b.getId()){
                    brokerResponseList.add(false);
                }
                else{
                    Boolean alternativeBrokerSuccess = true;
                    try{
                        log.info("대체발송 중계사: {}번-{}", b.getId(),msgBroker.get(b.getId()));
                        BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(),sendMsgProceessingDTO);
                        System.out.println("broker[대체발송] request: "+ brokerRequestLogDTO.toString());
                        ResponseEntity<Long> response = sendBrokerApi(sendMsgProceessingDTO.getBrokerId(),sendMsgProceessingDTO.getBrokerMsgDTO());
                    }
                    catch (CustomException e){
                        System.out.println(e);
                        log.info("*******************************************");
                        System.out.println("ERROR : BROKER - " + e.getErrorCode());
                        alternativeBrokerSuccess = false;
                        BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL,sendMsgProceessingDTO);
                        if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                            brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                        }
                        else if(e.getErrorCode() == ErrorCode.INVALID_PHONE){
                            brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                        }
                        System.out.println("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                        log.info("*******************************************");
                    }
                    finally {
                        if(alternativeBrokerSuccess){
                            brokerResponseList.add(true);
                            BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE,sendMsgProceessingDTO);
                            System.out.println("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                            break;
                        }
                        else{
                            brokerResponseList.add(false);
                        }
                    }
                }
            }
        }
            private ResponseEntity<Long> sendBrokerApi(Long brokerId, BrokerMsgDTO brokerMsgDTO){
                ResponseEntity<Long> responseEntity = null;
                if (brokerId == 1L) {
                    responseEntity=smsBroker1FeignClient.sendSms(msgBroker.get(brokerId),brokerMsgDTO);
                } else if (brokerId == 2L) {
                    responseEntity=smsBroker2FeignClient.sendSms(msgBroker.get(brokerId),brokerMsgDTO);
                } else if (brokerId == 3L) {
                    responseEntity=smsBroker3FeignClient.sendSms(msgBroker.get(brokerId),brokerMsgDTO);
                }
                return responseEntity;
            }
            private List<BrokerDTO> getMsgBrokerList(){
                List<Broker> BrokerList = readBrokerRepository.findBySendingType(SendingType.SMS);
                List<BrokerDTO> brokerDTOList = new ArrayList<>();
                BrokerList.forEach(broker -> {
                    brokerDTOList.add(mapper.convertValue(broker,BrokerDTO.class));
                });

                return brokerDTOList;
            }
}
