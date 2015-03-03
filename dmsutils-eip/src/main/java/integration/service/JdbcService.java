package integration.service;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Service
public class JdbcService {

    public void processMessage(Message message){
        System.out.println(message.toString());
    }
}
