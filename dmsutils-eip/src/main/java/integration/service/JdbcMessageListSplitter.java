package integration.service;

import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 3/4/2015.
 */

public class JdbcMessageListSplitter extends AbstractMessageSplitter{

    @Override
    protected Message[] splitMessage(final Message<?> message){
        System.out.println("\n\nInside splitMessage function");
        final ArrayList<StoreServiceMessage> payloadListContent = (ArrayList<StoreServiceMessage>)message.getPayload();
        System.out.println("Total items contained in payload = " + payloadListContent.size());
        Message[] splitMessages = new Message[payloadListContent.size()];

        for(int i = 0;i < payloadListContent.size();++i){
            final int index = i;
            splitMessages[i] = new Message<StoreServiceMessage>(){
                @Override
                public MessageHeaders getHeaders(){
                    System.out.println(message.getHeaders());
                    System.out.println("Headers on split = " + (Map<String,Object>)message.getHeaders().get(Integer.valueOf(index).toString()));
                    return new MessageHeaders((Map<String,Object>)message.getHeaders().get(Integer.valueOf(index).toString()));
                }

                @Override
                public StoreServiceMessage getPayload(){
                    return payloadListContent.get(index);
                }
            };
        }
        return splitMessages;
    }
}