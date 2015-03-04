package integration.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;
import ro.croco.integration.dms.toolkit.db.StoreServiceMessageDb;

import java.util.List;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Service
public class JdbcService {

    public  Message<List<StoreServiceMessage>> inspectResponse(final Message<List<StoreServiceMessage>> message){
        //System.out.println("Response = " + message.getPayload());
        return message;
    }

    public  Message<StoreServiceMessage> inspectSingleResponse(final Message<StoreServiceMessage> message){
        //System.out.println("Response = " + message.getPayload());
        return message;
    }

    
    public  Message<List<StoreServiceMessageDb>> processRequest(final Message<List<StoreServiceMessageDb>> message){
        System.out.println("\n Request 123 = \n" + message.getPayload());

        Message<List<StoreServiceMessageDb>> responseMessage = new Message<List<StoreServiceMessageDb>>() {
            @Override
            public MessageHeaders getHeaders(){
                return null;
            }

            @Override
            public List<StoreServiceMessageDb> getPayload() {
                System.out.println("\n \n Chestia ciudata -?>>>>>>" +  message.getPayload().get(0).getMessageID());
                return message.getPayload();
            }
        };
        System.out.println("\n \n Chestia ciudata -?>>>>>>" +  responseMessage.getPayload().get(0).getMessageID());
        return responseMessage;
    }

    public  Message<StoreServiceMessage> processSingleRequest(final Message<List<StoreServiceMessage>> message){
        //System.out.println("Request = " + message.getPayload());

        Message<List<StoreServiceMessage>> responseMessage = new Message<List<StoreServiceMessage>>() {
            @Override
            public MessageHeaders getHeaders(){
                return null;
            }

            @Override
            public List<StoreServiceMessage> getPayload() {
                return message.getPayload();
            }
        };
        return new GenericMessage<StoreServiceMessage>(message.getPayload().get(0));
    }

    public  Message<List<StoreServiceMessage>> afterUpdate(final Message<List<StoreServiceMessage>> message){
        System.out.println("After update = " + message.getPayload());
        return message;
    }
}
