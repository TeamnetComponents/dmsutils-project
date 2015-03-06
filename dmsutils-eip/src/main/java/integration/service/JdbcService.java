package integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.elo.ix.client.ActivityProject;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import ro.croco.integration.dms.toolkit.StoreService;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;
import ro.croco.integration.dms.toolkit.db.StoreServiceMessageDb;

import java.io.IOException;
import java.util.*;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Service
public class JdbcService{

    @Async
    public Message singleRequestProcess(Message message){
        System.out.println("\n\nInside singleRequestProcess function");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writeValueAsString(message.getPayload()));
            System.out.println(message.getHeaders());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return message;
    }

    public  Message<ArrayList<StoreServiceMessage>> processRequestBatch(final Message<ArrayList<LinkedCaseInsensitiveMap>> message){
        System.out.println("\n\nInside processRequestBatch function");
        System.out.println("Total rows number perceived in message = " + message.getPayload().size());

       Message<ArrayList<StoreServiceMessage>> responseMsg = new
               Message<ArrayList<StoreServiceMessage>>(){
                   @Override
                   public MessageHeaders getHeaders(){
                       System.out.println("first getHeaders call");
                       Map<String,Object> headersValues = new HashMap<String, Object>();
                       ArrayList<LinkedCaseInsensitiveMap> inputPayload = message.getPayload();

                       for(int i = 0;i < inputPayload.size();++i){
                           LinkedCaseInsensitiveMap row = inputPayload.get(i);
                           HashMap<String,Object> value = new HashMap<String, Object>();
                           value.put("MSG_ID",row.get("MSG_ID"));
                           value.put("MSG_CORRELATION_ID",row.get("MSG_CORRELATION_ID"));
                           value.put("MSG_DESTINATION",row.get("MSG_DESTINATION"));
                           value.put("MSG_DESTINATION",row.get("MSG_DESTINATION"));
                           value.put("MSG_REPLY_TO",row.get("MSG_REPLY_TO"));
                           value.put("MSG_EXPIRATION",row.get("MSG_EXPIRATION"));
                           value.put("MSG_PRIORITY",row.get("MSG_PRIORITY"));
                           value.put("PRC_STATUS",row.get("PRC_STATUS"));
                           value.put("PRC_ID",row.get("PRC_ID"));
                           headersValues.put(Integer.valueOf(i).toString(),value);
                       }
                       MessageHeaders headers = new MessageHeaders(headersValues);
                       System.out.println("beforeExit = " + headers.get("1"));
                       System.out.println("\n");
                       return headers;
                   }

                   @Override
                   public ArrayList<StoreServiceMessage> getPayload(){
                       ArrayList<StoreServiceMessage> newPayload = new ArrayList<StoreServiceMessage>();
                       ObjectMapper objectMapper = new ObjectMapper();
                       ArrayList<LinkedCaseInsensitiveMap> inputPayload = message.getPayload();
                       for(int i = 0;i < inputPayload.size();++i){
                           LinkedCaseInsensitiveMap row = inputPayload.get(i);
                           try{
                               StoreServiceMessage msgContent = objectMapper.readValue((String)row.get("MSG_CONTENT"),StoreServiceMessage.class);
                               newPayload.add(msgContent);
                           }
                           catch (IOException onJacksonEx){
                               onJacksonEx.printStackTrace();
                           }
                       }
                       return newPayload;
                   }
               };
        return responseMsg;
    }
}