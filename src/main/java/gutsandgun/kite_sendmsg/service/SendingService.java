package gutsandgun.kite_sendmsg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendmsg.dto.*;
import gutsandgun.kite_sendmsg.dto.log.BrokerRequestLogDTO;
import gutsandgun.kite_sendmsg.dto.log.BrokerResponseLogDTO;
import gutsandgun.kite_sendmsg.dto.log.MissingSendingIdLogDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.BrokerMsgDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.ReplaceSendingBodyDTO;
import gutsandgun.kite_sendmsg.dto.sendMsg.SendMsgProceessingDTO;
import gutsandgun.kite_sendmsg.entity.read.Broker;
import gutsandgun.kite_sendmsg.exception.ConsumerException;
import gutsandgun.kite_sendmsg.exception.CustomException;
import gutsandgun.kite_sendmsg.exception.ErrorCode;
import gutsandgun.kite_sendmsg.feignClients.SendingFeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsBroker1FeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsBroker2FeignClient;
import gutsandgun.kite_sendmsg.feignClients.SmsBroker3FeignClient;
import gutsandgun.kite_sendmsg.publisher.RabbitMQProducer;
import gutsandgun.kite_sendmsg.repository.read.ReadBrokerRepository;
import gutsandgun.kite_sendmsg.type.FailReason;
import gutsandgun.kite_sendmsg.type.SendingStatus;
import gutsandgun.kite_sendmsg.type.SendingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    SendingCache sendingCache;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    public void sendMsgProcessing(Long brokerId,SendMsgProceessingDTO sendMsgProceessingDTO){
            //2.sending ?????? ??????
            SendingDto sendingDto = getSending(sendMsgProceessingDTO);
            if(sendingDto!=null){
                log.info("-----------------------------");
                //3.broker msg????????? (with msg ??????)
                sendMsgProceessingDTO.setSendingDto(sendingDto);
                sendMsgProceessingDTO.setBrokerMsgDTO();
                log.info("-----------------------------");
                //4.??????
                BrokerResponseLogDTO brokerResponseLogDTO = sendBroker(sendMsgProceessingDTO);
                //5.???????????? (?????????/?????????)
                if(brokerResponseLogDTO.getSuccess().equals(SendingStatus.FAIL)){
                    switch (brokerResponseLogDTO.getFailReason()){
                        case BAD_REQUEST :
                            rabbitMQProducer.logSendQueue("broker[????????????] response log: "+brokerResponseLogDTO.toString());
                            log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                            log.info("*******************************************");
                            log.info("-----------------------------");
                            alternativeSendBroker(sendMsgProceessingDTO);
                            break;
                        case INVALID_PHONE:
                            log.info(sendMsgProceessingDTO.getSendingDto().getReplaceYn());
                            if(sendMsgProceessingDTO.getSendingDto().getReplaceYn().equals("Y")){
                                log.info("????????? ?????? ?????? ??????");
                                rabbitMQProducer.logSendQueue("broker[????????????] response log: "+brokerResponseLogDTO.toString());
                                log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                                log.info("*******************************************");
                                log.info("-----------------------------");
                                alternativeSendEmail(sendMsgProceessingDTO);
                            }
                            if(sendMsgProceessingDTO.getSendingDto().getReplaceYn().equals("N")){
                                log.info("????????? ?????? ?????? X");
                                brokerResponseLogDTO.setLast(true);
                                rabbitMQProducer.logSendQueue("broker[????????????] response log: "+brokerResponseLogDTO.toString());
                                log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                                log.info("*******************************************");
                                log.info("-----------------------------");
                            }
                            break;
                    }
                }
                else{
                    rabbitMQProducer.logSendQueue("broker[????????????] response log: "+brokerResponseLogDTO.toString());
                    log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                    log.info("-----------------------------");
                }
            }
    }




    private SendingDto getSending(SendMsgProceessingDTO sendMsgProceessingDTO){
        try {
            return objectMapper.readValue(sendingCache.getSendingDto(sendMsgProceessingDTO.getSendingId()), SendingDto.class);
        }
        catch(ConsumerException e){
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending ?????? DB ??? ??????");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendMsgProceessingDTO);
                rabbitMQProducer.logSendQueue("log: " + missingSendingIdLogDTO.toString());
                log.info("log: " + missingSendingIdLogDTO.toString());
                return null;
            }
            log.info("*******************************************");
        } catch (JsonProcessingException e) {
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending ?????? DB ??? ??????2 (parsing error)");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendMsgProceessingDTO);
                rabbitMQProducer.logSendQueue("log: " + missingSendingIdLogDTO.toString());
                log.info("log: " + missingSendingIdLogDTO.toString());
            }
            log.info("*******************************************");
            return null;
        }
        return null;
    }


    public BrokerResponseLogDTO sendBroker(SendMsgProceessingDTO sendMsgProceessingDTO){
            BrokerResponseLogDTO brokerResponseLogDTO = null;
            log.info("4. Send broker: {}",sendMsgProceessingDTO.getBrokerMsgDTO());
            BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(sendMsgProceessingDTO.getBrokerId(),sendMsgProceessingDTO);
            rabbitMQProducer.logSendQueue("broker[????????????] request log: "+ brokerRequestLogDTO.toString());
            log.info("broker[????????????] request log: "+ brokerRequestLogDTO.toString());
            try {
                ResponseEntity<Long> response = sendBrokerApi(sendMsgProceessingDTO.getBrokerId(),sendMsgProceessingDTO.getBrokerMsgDTO());
            }
            catch (CustomException e){
                log.info("*******************************************");
                log.info("ERROR : BROKER - " + e.getErrorCode());
                brokerResponseLogDTO = new BrokerResponseLogDTO(sendMsgProceessingDTO.getBrokerId(), SendingStatus.FAIL,sendMsgProceessingDTO);
                if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                    //1. ????????? ??????
                    brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                }
                else if(e.getErrorCode() == ErrorCode.INVALID_PHONE){
                    //2.????????????/???????????? ?????? ?????? ??????
                    brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                }
                else{
                    //other????????? ??????????????????????
                }
            }
            finally {
                if(brokerResponseLogDTO==null){
                    brokerResponseLogDTO = new BrokerResponseLogDTO(sendMsgProceessingDTO.getBrokerId(), SendingStatus.COMPLETE,sendMsgProceessingDTO);
                    brokerResponseLogDTO.setLast(true);
                }
            }
            log.info("-----------------------------");
            return brokerResponseLogDTO;
        }

        public void alternativeSendEmail(SendMsgProceessingDTO sendMsgProceessingDTO){
            ReplaceSendingBodyDTO replaceSendingBodyDTO = new ReplaceSendingBodyDTO(sendMsgProceessingDTO.getSendingId(),sendMsgProceessingDTO.getTxId());
            log.info("5-1. ????????? ????????????");
            log.info("api[????????????] request: {}",replaceSendingBodyDTO);
            ResponseEntity<String> response = sendingFeignClient.sendSms(replaceSendingBodyDTO);
            log.info("api[????????????] response: {}",response.getStatusCode());
        }
        public void alternativeSendBroker(SendMsgProceessingDTO sendMsgProceessingDTO){
            log.info("5-2. ????????? ????????????");

            //broker ?????? ????????????
            List<BrokerDTO> brokerDTOList = getMsgBrokerList();
            ArrayList<Boolean> brokerResponseList = new ArrayList<Boolean>();
            log.info("brokerList:{}",brokerDTOList);
            log.info("-----------------------------");

            int brokerSendingCount = 1;
            //?????? ?????? ??????(sending queue)
            for (BrokerDTO b : brokerDTOList){
                //???????????? false??????
                if(sendMsgProceessingDTO.getBrokerId() == b.getId()){
                    brokerResponseList.add(false);
                }
                else{
                    Boolean alternativeBrokerSuccess = true;
                    try{
                        log.info("???????????? ?????????: {}???-{}", b.getId(),msgBroker.get(b.getId()));
                        BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(),sendMsgProceessingDTO);
                        brokerSendingCount+=1;
                        rabbitMQProducer.logSendQueue("broker[????????????] request: "+ brokerRequestLogDTO.toString());
                        log.info("broker[????????????] request: "+ brokerRequestLogDTO.toString());
                        ResponseEntity<Long> response = sendBrokerApi(b.getId(),sendMsgProceessingDTO.getBrokerMsgDTO());
                    }
                    catch (CustomException e){
                        //System.out.println(e);
                        log.info("*******************************************");
                        log.info("ERROR : BROKER - " + e.getErrorCode());
                        alternativeBrokerSuccess = false;
                        BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL,sendMsgProceessingDTO);
                        if(brokerSendingCount==3){
                            //??? ?????? ?????? ?????????
                            brokerResponseLogDTO.setLast(true);
                        }
                        if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                            brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                        }
                        else if(e.getErrorCode() == ErrorCode.INVALID_PHONE){
                            brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                        }
                        rabbitMQProducer.logSendQueue("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                        log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                        log.info("*******************************************");
                    }
                    finally {
                        if(alternativeBrokerSuccess){
                            brokerResponseList.add(true);
                            BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE,sendMsgProceessingDTO);
                            brokerResponseLogDTO.setLast(true);
                            rabbitMQProducer.logSendQueue("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                            log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
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
