package integration.router;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ro.croco.integration.dms.toolkit.jms.JmsMessageStructure;

//import javax.jms.Message;

/**
 * Created by hanna.botar on 7/10/2014.
 */
@Configuration
public class ActionRouter {

    private static final String SUFFIX = "RouteChannel";

//    public String routeForMethod(@Payload Object payload) {
    public String routeForMethod(Message message) {

        System.out.println(" ----------- ActionRouter ------------");


        Object payload = message.getPayload();

        System.out.println(payload.toString());

        JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;
        String caz = jmsMessageStructure.getMethod();

        System.out.println(" ------------- CAZ -----------" + caz);
        return caz + SUFFIX;
    }


}
