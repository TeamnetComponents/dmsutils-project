package ro.tn.components.dmsutils.eip;

import integration.config.common.DmsConfig;
import integration.config.IntegrationConfig;
import integration.config.JdbcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Razvan.Ionescu on 3/3/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JdbcConfig.class,DmsConfig.class,IntegrationConfig.class})
public class JdbcIntegrationTest{

    @Test
    public void testIntegration() throws Exception{
        while(true){
//            Message message = queueChannel.receive(1000);
//            if (message != null) {
//                System.out.println(message);
//            }
        }
    }


}
