package integration.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;

import java.util.Map;

/**
 * Created by Razvan.Ionescu on 3/3/2015.
 */
@Service
public class JdbcMessageTransformer{

    public Message<StoreServiceMessage> toStoreServiceMessage(final Message message) {
        try {
            Message<StoreServiceMessage> trasformedMessage = new Message<StoreServiceMessage>() {
                @Override
                public MessageHeaders getHeaders() {
                    return null;
                }

                @Override
                public StoreServiceMessage getPayload() {
                    StoreServiceMessage ssm = (StoreServiceMessage) ((Map) message.getPayload()).get("MSG_CONTENT");
                    return ssm;
                }
            };
            System.out.println("pe bune");
            //System.out.println(trasformedMessage.getPayload().toString());
            return trasformedMessage;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
    
}
