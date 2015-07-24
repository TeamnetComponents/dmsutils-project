package ro.tn.components.dmsutils.eip;

import integration.config.common.DmsConfig;
import integration.config.IntegrationConfig;
import integration.config.jms.JmsConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by alexandruk on 6/12/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JmsConfig.class,DmsConfig.class,IntegrationConfig.class})
public class JmsIntegrationTest {

    @Test
    public void testIntegration() throws Exception{
        while(true){
            //se porneste automat de catre spring pollerul din configurare si ruleaza atat timp cat ruleaza testul
        }
    }
}
